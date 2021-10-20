package com.hengtiansoft.strategy.config.py4j;

import com.hengtiansoft.strategy.entrypoint.EntryPoint;
import com.hengtiansoft.strategy.gateway.JavaGatewayServer;
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
    public EntryPoint entryPoint() {
        return new EntryPoint();
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

    public String getAddress() {
        return properties.getDefaultAddress();
    }

    public int getPort() {
        return properties.getPort();
    }
}
