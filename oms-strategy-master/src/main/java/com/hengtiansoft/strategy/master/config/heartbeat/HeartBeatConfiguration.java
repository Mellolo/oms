package com.hengtiansoft.strategy.master.config.heartbeat;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Configuration
public class HeartBeatConfiguration {

    @Autowired
    private ThreadPoolTaskExecutor poolTaskExecutor;

    @Bean
    HeartBeater heartBeater() {
        return new HeartBeater();
    }

}

@Slf4j
class HeartBeater implements InitializingBean {

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
        RLock lock = redissonClient.getLock("heartbeat");
        while(true) {
            log.info("heartbeat");
            try {
                if (lock.tryLock(0,2, TimeUnit.SECONDS)) {
                    RReadWriteLock rwlock = redissonClient.getReadWriteLock("update");
                    try {
                        rwlock.writeLock().lock(2, TimeUnit.SECONDS);

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
                            strategyDeleted = hostPortService.selectStrategyByHostPort(diffDown);
                            hostPortService.deleteHostPort(diffDown);
                        }

                        // 开启副本备份
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
                            //todo: 并行turnDuplicate2Strategy
                            for(Map.Entry<String, List<String>> entry: duplicatesToTurnUp.entrySet()) {
                                String strategyId = entry.getKey();
                                List<String> hostPortList = entry.getValue();
                                Collections.shuffle(hostPortList);
                                boolean res = false;
                                for(String hostPort: hostPortList) {
                                    if(
                                            registerService.turnDuplicate2Strategy(hostPort, strategyId)
                                    ) {
                                        res = true;
                                        break;
                                    }
                                }
                                if(!res) {
                                    log.error("HeartBeat Error: cannot turnDuplicate2Strategy "+strategyId);
                                }
                            }
                        }

                        // 刷新扩容的机器
                        //todo: 并行刷新
                        if(!diffUp.isEmpty()) {
                            for(String hostPort: diffUp) {
                                registerService.refresh(hostPort);
                            }
                            stringRedisTemplate.opsForSet().add("serverSet", diffUp.toArray(new String[0]));
                        }

                        // 更新服务器列表的时间
                        stringRedisTemplate.opsForValue().set("serverSetUpdateTime", String.valueOf(System.currentTimeMillis()));
                    }
                    catch (Exception e) {
                        log.error(e.toString());
                    }
                    finally {
                        lock.unlock();
                        rwlock.writeLock().unlock();
                    }
                }
            }
            catch (Exception e) {
                log.error("Error heartbeat " + e.toString());
            }

            try {
                Thread.sleep(1000);
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
