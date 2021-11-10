package com.hengtiansoft.strategy.master.controller;

import com.google.common.collect.Lists;
import com.hengtiansoft.strategy.master.config.DuplicateProperties;
import com.hengtiansoft.strategy.master.model.HostPortCountModel;
import com.hengtiansoft.strategy.master.model.StrategyHostPortModel;
import com.hengtiansoft.strategy.master.service.HostPortService;
import com.hengtiansoft.strategy.master.service.RegisterService;
import com.hengtiansoft.strategy.master.service.RunningStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@EnableConfigurationProperties(DuplicateProperties.class)
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    @Autowired
    private HostPortService hostPortService;

    @Autowired
    private RunningStrategyService runningStrategyService;

    @Autowired
    @Qualifier("stringRedisTemplate")
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @PostMapping("register")
    public String register(int codeId, String userId, String[] accounts) {
        // 缓存着的服务器列表
        Set<String> cachedServerSet = stringRedisTemplate.opsForSet().members("serverSet");
        if(cachedServerSet==null) {
            cachedServerSet = new HashSet<>();
        }

        // 注册策略
        String strategyId = UUID.randomUUID().toString();
        List<HostPortCountModel> hostPortCountModels = hostPortService.selectStrategyHostPortCountByHostPort(cachedServerSet);
        Collections.sort(hostPortCountModels);
        String strategyHostPort = null;
        for(HostPortCountModel hostPortCountModel: hostPortCountModels) {
            if(
                    registerService.register(
                            hostPortCountModel.getHostPort(),
                            strategyId,
                            codeId,
                            userId,
                            accounts
                    )
            ) {
                strategyHostPort = hostPortCountModel.getHostPort();
                break;
            }
        }
        if(StringUtils.isBlank(strategyHostPort)) {
            return "Register fails";
        }

        // 添加副本
        // todo: 并行添加副本
//        threadPoolTaskExecutor.execute(new FutureTask<>(new Callable<Boolean>() {
//            @Override
//            public Boolean call() {
//                return null;
//            }
//        }));
        hostPortCountModels = hostPortService.selectDuplicateHostPortCountByHostPort(cachedServerSet);
        Collections.sort(hostPortCountModels);
        int requiredNum = 2;
        int duplicateNum = 0;
        for(HostPortCountModel hostPortCountModel: hostPortCountModels) {
            if(strategyHostPort.equals(hostPortCountModel.getHostPort())) {
                continue;
            }
            if(duplicateNum>=requiredNum){
                break;
            }
            if(
                    registerService.addDuplicate(
                        hostPortCountModel.getHostPort(),
                        strategyId
                    )
            ) {
                duplicateNum++;
            }
        }
        runningStrategyService.turnUp(strategyId);
        return String.format("Register succeed with %d duplicates", duplicateNum);
    }

    @DeleteMapping("unregister")
    public String unregister(String strategyId) {
        while(true) {
            releaseForHeartBeat();
            if(runningStrategyService.isUp(strategyId)) {
                RReadWriteLock rwlock = redissonClient.getReadWriteLock("update");
                try {
                    if(rwlock.readLock().tryLock(0, 1, TimeUnit.SECONDS)) {
                        try {
                            StrategyHostPortModel strategyHostPortModel = hostPortService.selectStrategyHostPortById(strategyId);
                            registerService.unregister(strategyHostPortModel.getHostPort(), strategyId);

                            // todo: 并行remove
                            List<StrategyHostPortModel> duplicateHostPortModels = hostPortService.selectDuplicateHostPortById(
                                    Lists.newArrayList(strategyId)
                            );
                            for(StrategyHostPortModel duplicateHostPortModel: duplicateHostPortModels) {
                                registerService.removeDuplicate(duplicateHostPortModel.getHostPort(), strategyId);
                            }
                            return "Unregister succeed";
                        }
                        finally {
                            rwlock.readLock().unlock();
                        }
                    }
                }
                catch (Exception e) {
                    log.error(e.toString());
                    return "Unregister fails";
                }
            }
            else {
                return "Unregister fails";
            }
        }
    }

    private void releaseForHeartBeat() {
        while(true) {
            String serverSetUpdateTimeStr = stringRedisTemplate.opsForValue().get("serverSetUpdateTime");
            if(serverSetUpdateTimeStr!=null) {
                long serverSetUpdateTime = Long.valueOf(serverSetUpdateTimeStr);
                if(System.currentTimeMillis() - serverSetUpdateTime < 1000) {
                    break;
                }
            }
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                log.error("Error releaseForHeartBeat Thread.sleep");
            }
        }
    }

}
