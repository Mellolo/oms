package com.hengtiansoft.strategy.master.controller;

import com.hengtiansoft.strategy.master.model.HostPortCountModel;
import com.hengtiansoft.strategy.master.service.HostPortService;
import com.hengtiansoft.strategy.master.service.RegisterService;
import com.hengtiansoft.strategy.master.service.RunningStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@RestController
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

    @GetMapping("register")
    public String register(int codeId, String userId, String[] accounts) {
        // 缓存着的服务器列表
        Set<String> cachedServerSet = stringRedisTemplate.opsForSet().members("serverSet");
        if(cachedServerSet==null) {
            cachedServerSet = new HashSet<>();
        }

        // 注册策略
        // todo: 空机器数据库选不出来
        String strategyId = UUID.randomUUID().toString();
        List<HostPortCountModel> hostPortCountModels = hostPortService.selectStrategyHostport(new ArrayList<>(cachedServerSet));
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
            return "Fail";
        }

        // 添加副本
        // todo: 空机器数据库选不出来
        hostPortCountModels = hostPortService.selectDuplicateHostport(new ArrayList<>(cachedServerSet));
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
        return String.format("Succeed with %d duplicates", duplicateNum);
    }

    @GetMapping("unregister")
    public void unregister(String strategyId) {
        while(true) {
            releaseForHeartBeat();
            if(runningStrategyService.isUp(strategyId)) {
                RReadWriteLock rwlock = redissonClient.getReadWriteLock("update");
                if(rwlock.readLock().tryLock()) {
                    try {
                        // todo: 根据表里的hostport找到特定的机器删除duplicates和strategy
                    }
                    finally {
                        rwlock.readLock().unlock();
                    }
                    return;
                }
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
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("Error releaseForHeartBeat Thread.sleep");
            }
        }
    }

}
