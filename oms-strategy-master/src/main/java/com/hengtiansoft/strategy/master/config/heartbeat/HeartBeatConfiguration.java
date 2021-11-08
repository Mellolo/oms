package com.hengtiansoft.strategy.master.config.heartbeat;

import com.hengtiansoft.strategy.master.feign.StrategyService;
import com.hengtiansoft.strategy.master.service.HostPortService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Configuration
public class HeartBeatConfiguration {

    @Autowired
    private ThreadPoolTaskExecutor poolTaskExecutor;

    @Bean
    HeartBeater heartBeater() {
        HeartBeater heartBeater = new HeartBeater();
        poolTaskExecutor.execute(heartBeater);
        return heartBeater;
    }

}

@Slf4j
class HeartBeater implements Runnable {

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
    StrategyService strategyService;

    private void heartbeat() {
        RLock lock = redissonClient.getLock("heartbeat");
        while(true) {
            if (lock.tryLock()) {
                try {
                    RReadWriteLock rwlock = redissonClient.getReadWriteLock("update");
                    rwlock.writeLock().lock();

                    // 缓存着的服务器列表
                    Set<String> cachedServerSet = stringRedisTemplate.opsForSet().members("serverSet");
                    if(cachedServerSet==null) {
                        cachedServerSet = new HashSet<>();
                    }

                    // 现有的可访问服务器列表
                    List<ServiceInstance> serviceInstances = discoveryClient.getInstances("oms-strategy-biz");
                    Set<String> serverSet = new HashSet<>();
                    for(ServiceInstance serviceInstance : serviceInstances) {
                        serverSet.add(serviceInstance.getHost()+":"+serviceInstance.getPort());
                    }

                    // 挂了的部分
                    Set<String> diffDown = new HashSet<>(cachedServerSet);
                    diffDown.removeAll(serverSet);

                    // 扩容的部分
                    Set<String> diffUp = new HashSet<>(serverSet);
                    diffUp.removeAll(cachedServerSet);

                    // 剩余的部分
                    Set<String> intersection = new HashSet<>(serverSet);
                    intersection.retainAll(cachedServerSet);

                    // 删除所有挂了的部分
                    stringRedisTemplate.opsForSet().remove("serverSet", diffDown.toArray());
                    hostPortService.deleteHostPort(new ArrayList<>(diffUp));
                    // todo: turnDuplicatesUp

                    // 刷新扩容的机器
                    strategyService.refresh();
                    stringRedisTemplate.opsForSet().add("serverSet", diffUp.toArray(new String[0]));

                    // 更新服务器列表的时间
                    stringRedisTemplate.opsForValue().set("serverSetUpdateTime", String.valueOf(System.currentTimeMillis()));

                    rwlock.writeLock().unlock();

                }
                finally {
                    lock.unlock();
                }
            }
            else {
                try {
                    Thread.sleep(1000);
                }
                catch (Exception e) {
                    log.error("Error heartbeat sleep");
                }
            }
        }
    }

    @Override
    public void run() {
        heartbeat();
    }
}
