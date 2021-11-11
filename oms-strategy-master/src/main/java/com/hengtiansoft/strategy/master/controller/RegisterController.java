package com.hengtiansoft.strategy.master.controller;

import com.google.common.collect.Lists;
import com.hengtiansoft.strategy.master.config.DuplicateProperties;
import com.hengtiansoft.strategy.master.model.HostPortCountModel;
import com.hengtiansoft.strategy.master.model.StrategyHostPortModel;
import com.hengtiansoft.strategy.master.service.HostPortService;
import com.hengtiansoft.strategy.master.service.RegisterService;
import com.hengtiansoft.strategy.master.service.RunningStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
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
import java.util.concurrent.*;

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
    ThreadPoolTaskExecutor poolTaskExecutor;

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

        // 并行添加副本
        int requiredNum = 2;
        int duplicateNum = 0;
        hostPortCountModels = hostPortService.selectDuplicateHostPortCountByHostPort(cachedServerSet);
        Collections.sort(hostPortCountModels);
        for(int i=0;i<hostPortCountModels.size();i+=requiredNum) {
            List<Future<Boolean>> futureList = new ArrayList<>();
            for(int j=i;j<i+requiredNum && j<hostPortCountModels.size();j++) {
                HostPortCountModel hostPortCountModel = hostPortCountModels.get(j);
                if(strategyHostPort.equals(hostPortCountModel.getHostPort())) {
                    continue;
                }
                FutureTask<Boolean> future = new FutureTask<>(
                        new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                return registerService.addDuplicate(hostPortCountModel.getHostPort(), strategyId);
                            }
                        }
                );
                poolTaskExecutor.execute(future);
                futureList.add(future);
            }
            for(Future<Boolean> future: futureList) {
                try {
                    if(BooleanUtils.isTrue(future.get())) {
                        requiredNum--;
                        duplicateNum++;
                    }
                }
                catch (Exception e) {
                    log.error("Error HearBeat refreshFuture: %s", e);
                }
            }
            if(requiredNum<=0){
                break;
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
                            if(strategyHostPortModel!=null && StringUtils.isNotBlank(strategyHostPortModel.getHostPort())) {
                                registerService.unregister(strategyHostPortModel.getHostPort(), strategyId);
                            }

                            List<StrategyHostPortModel> duplicateHostPortModels = hostPortService.selectDuplicateHostPortById(
                                    Lists.newArrayList(strategyId)
                            );
                            for(StrategyHostPortModel duplicateHostPortModel: duplicateHostPortModels) {
                                poolTaskExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        registerService.removeDuplicate(duplicateHostPortModel.getHostPort(), strategyId);
                                    }
                                });
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
