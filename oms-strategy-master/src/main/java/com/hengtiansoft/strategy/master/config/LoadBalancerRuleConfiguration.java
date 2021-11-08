package com.hengtiansoft.strategy.master.config;

import com.hengtiansoft.strategy.master.utils.StrategyUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class LoadBalancerRuleConfiguration {

    @Bean
    public IRule ribbonRule() {
        return new ConsistentHashRule();
    }

    public static class ConsistentHashRule extends AbstractLoadBalancerRule {

        private final IRule DEFAULT_RULE = new RoundRobinRule();

        private Logger log = LoggerFactory.getLogger(ConsistentHashRule.class);

        private Server choose(ILoadBalancer lb, String hostPort) {
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

                for(Server s: allServers) {
                    if (s.getHostPort().equals(hostPort)) {
                        server = s;
                        break;
                    }
                }


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
            String hostPort = StrategyUtils.getHostPort();
            if(StringUtils.isNotBlank(hostPort)) {
                return choose(getLoadBalancer(), hostPort);
            }
            else {
                DEFAULT_RULE.setLoadBalancer(getLoadBalancer());
                return DEFAULT_RULE.choose(key);
            }
        }

        @Override
        public void initWithNiwsConfig(IClientConfig clientConfig) {}

    }
 
}