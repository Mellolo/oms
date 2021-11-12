package com.hengtiansoft.strategy.master.component.heartbeat;

import com.google.common.collect.Lists;
import com.hengtiansoft.strategy.master.model.StrategyHostPortModel;
import com.hengtiansoft.strategy.master.service.HostPortService;
import com.hengtiansoft.strategy.master.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HeartBeater implements InitializingBean {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    @Qualifier("stringRedisTemplate")
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    HostPortService hostPortService;

    @Autowired
    RegisterService registerService;

    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private void heartbeat() {
        while(true) {
            long start = System.currentTimeMillis();
            try {
                RLock lock = redissonClient.getLock("heartbeat");
                if (lock.tryLock(0,6, TimeUnit.SECONDS)) {
                    try {
                        RReadWriteLock rwlock = redissonClient.getReadWriteLock("update");
                        rwlock.writeLock().lock(6, TimeUnit.SECONDS);
                        try {
                            // 缓存着的服务器列表
                            Set<String> cachedServerSet = stringRedisTemplate.opsForSet().members("serverSet");
                            if(cachedServerSet==null) {
                                cachedServerSet = new HashSet<>();
                            }

                            // 现有的可访问服务器列表
                            List<ServiceInstance> serviceInstances = discoveryClient.getInstances("oms-strategy-biz");
                            Set<String> serverSet = serviceInstances.stream()
                                    .map(s->s.getHost()+":"+s.getPort())
                                    .collect(Collectors.toSet());

                            // 挂了的部分
                            Set<String> diffDown = new HashSet<>(cachedServerSet);
                            diffDown.removeAll(serverSet);

                            // 扩容的部分
                            Set<String> diffUp = new HashSet<>(serverSet);
                            diffUp.removeAll(cachedServerSet);

                            // 删除所有挂了的部分
                            List<String> strategyDeleted = null;
                            if(!diffDown.isEmpty()) {
                                stringRedisTemplate.opsForSet().remove("serverSet", diffDown.toArray());
                                // 删之前获取挂了的strategyId
                                strategyDeleted = hostPortService.selectStrategyByHostPort(diffDown);
                                hostPortService.deleteHostPort(diffDown);
                            }

                            // 并行刷新扩容的机器
                            List<Future<Boolean>> refreshFutureList = new ArrayList<>();
                            if(!diffUp.isEmpty()) {
                                for(String hostPort: diffUp) {
                                    FutureTask<Boolean> future = new FutureTask<>(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    registerService.refresh(hostPort);
                                                    stringRedisTemplate.opsForSet().add("serverSet", hostPort);
                                                }
                                            },
                                            true
                                    );
                                    threadPoolTaskExecutor.execute(future);
                                    refreshFutureList.add(future);
                                }
                            }

                            // 并行开启副本备份运行策略
                            List<Future<Boolean>> turnUpFutureList = new ArrayList<>();
                            if(!CollectionUtils.isEmpty(strategyDeleted)) {
                                List<StrategyHostPortModel> duplicateHostPorts = hostPortService.selectDuplicateHostPortById(strategyDeleted);
                                Map<String, List<String>> duplicatesToTurnUp = duplicateHostPorts.stream().collect(
                                        Collectors.toMap(
                                                StrategyHostPortModel::getStrategyId,
                                                h -> Lists.newArrayList(h.getHostPort()),
                                                (List<String> l1, List<String>  l2) -> {
                                                    l1.addAll(l2);
                                                    return l1;
                                                }
                                        )
                                );
                                // 并行turnDuplicate2Strategy
                                for(Map.Entry<String, List<String>> entry: duplicatesToTurnUp.entrySet()) {
                                    // 执行turnDuplicate2Strategy
                                    FutureTask<Boolean> future = new FutureTask<>(
                                            new Callable<Boolean>() {
                                                @Override
                                                public Boolean call() throws Exception {
                                                    String strategyId = entry.getKey();
                                                    List<String> hostPortList = entry.getValue();
                                                    Collections.shuffle(hostPortList);
                                                    for(String hostPort: hostPortList) {
                                                        if(
                                                                registerService.turnDuplicate2Strategy(hostPort, strategyId)
                                                        ) {
                                                            return true;
                                                        }
                                                    }
                                                    log.error("HeartBeat Error: cannot turnDuplicate2Strategy "+strategyId);
                                                    return false;
                                                }
                                            }
                                    );
                                    threadPoolTaskExecutor.execute(future);
                                    turnUpFutureList.add(future);
                                }
                            }

                            // 等待所有刷新执行完毕
                            for(Future<Boolean> future: refreshFutureList) {
                                try {
                                    future.get();
                                }
                                catch (Exception e) {
                                    log.error("Error HearBeat refreshFuture: ", e);
                                }
                            }
                            // 等待所有副本开启操作执行完毕
                            for(Future<Boolean> future: turnUpFutureList) {
                                try {
                                    future.get();
                                }
                                catch (Exception e) {
                                    log.error("Error HearBeat turnUpFuture: ", e);
                                }
                            }

                            // 更新服务器列表的时间
                            stringRedisTemplate.opsForValue().set("serverSetUpdateTime", String.valueOf(System.currentTimeMillis()));
                        }
                        catch (Exception e) {
                            log.error("Error HearBeat: ", e);
                        }
                        finally {
                            rwlock.writeLock().unlock();
                        }
                    }
                    finally {
                        lock.unlock();
                    }
                }
            }
            catch (Exception e) {
                log.error("Error heartbeat " + e.toString());
            }
            long end = System.currentTimeMillis();
            //log.info(String.format("heartbeat: %d", end - start));

            try {
                long frequency = 200;
                if((end - start)<frequency) {
                    Thread.sleep(frequency - (end - start));
                }
            }
            catch (Exception e) {
                log.error("Error heartbeat sleep");
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        threadPoolTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                heartbeat();
            }
        });
    }
}
