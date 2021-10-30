package com.hengtiansoft.strategy.config.py4j;

import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import com.hengtiansoft.strategy.bo.gateway.EntryPoint;
import com.hengtiansoft.strategy.bo.gateway.JavaGatewayServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import py4j.GatewayServer;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayConfiguration {

    private GatewayProperties properties;

    @Autowired
    Map<String, RunningStrategy> strategyMap;

    public GatewayConfiguration(GatewayProperties properties) {
        this.properties = properties;
    }

    @Bean
    public EntryPoint entryPoint() {
        return new EntryPoint(strategyMap);
    }

    @Bean
    public GatewayServer gatewayServer(){
        GatewayServer gatewayServer = new JavaGatewayServer(
                entryPoint(),
                properties.getPort(),
                properties.getPythonPort(),
                properties.getDefaultAddress()
        );
        gatewayServer.start();
        return gatewayServer;
    }
}
