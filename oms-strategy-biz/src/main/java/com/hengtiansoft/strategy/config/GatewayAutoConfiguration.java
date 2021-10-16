package com.hengtiansoft.strategy.config;

import com.hengtiansoft.strategy.entrypoint.EntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import py4j.GatewayServer;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayAutoConfiguration {

    private GatewayProperties properties;

    public GatewayAutoConfiguration(GatewayProperties properties) {
        this.properties = properties;
    }

    @Bean
    public GatewayServer gatewayServer(EntryPoint entryPoint){
        GatewayServer gatewayServer =  new GatewayServer(entryPoint, properties.getPort());
        gatewayServer.start();
        return gatewayServer;
    }
}
