package com.hengtiansoft.strategyfacade.config;

import com.google.common.hash.Hashing;
import com.hengtiansoft.strategyfacade.util.StrategyUtil;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Configuration
public class LoadBalancerRuleConfiguration {
    @Bean
    public IRule ribbonRule() {
        return new ConsistentHashRule();
    }

    public static class ConsistentHashRule extends AbstractLoadBalancerRule {

        private final IRule DEFAULT_RULE = new RoundRobinRule(getLoadBalancer());

        private Logger log = LoggerFactory.getLogger(ConsistentHashRule.class);

        private Server choose(ILoadBalancer lb, String strategyId) {
            if (lb == null) {
                log.warn("no load balancer");
                return null;
            }

            Server server = null;
            int count = 0;
            while (server == null && count++ < 10) {
                List<Server> reachableServers = lb.getReachableServers();
                List<Server> allServers = lb.getAllServers();
                int upCount = reachableServers.size();
                int serverCount = allServers.size();
                if ((upCount == 0) || (serverCount == 0)) {
                    log.warn("No up servers available from load balancer: " + lb);
                    return null;
                }

                int hashcode = strategyId.hashCode();
                int model = Hashing.consistentHash(hashcode, serverCount); //一致性哈希，直接返回第几个数

                server = allServers.get(model);

                if (server == null) {
                    /* Transient. */
                    Thread.yield();
                    continue;
                }

                if (server.isAlive() && (server.isReadyToServe())) {
                    return (server);
                }

                // Next.
                server = null;
            }

            if (count >= 10) {
                log.warn("No available alive servers after 10 tries from load balancer: "
                        + lb);
            }
            return server;
        }

        @Override
        public Server choose(Object key) {
            //获取请求strategyId
            String strategyId = StrategyUtil.getStrategyId();
            if(StringUtils.isNotBlank(strategyId)) {
                return choose(getLoadBalancer(), strategyId);
            }
            else {
                return DEFAULT_RULE.choose(key);
            }
        }

        @Override
        public void initWithNiwsConfig(IClientConfig clientConfig) {}

    }
 
}