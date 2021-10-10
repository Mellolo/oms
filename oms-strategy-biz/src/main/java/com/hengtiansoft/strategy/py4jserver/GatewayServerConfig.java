package com.hengtiansoft.strategy.py4jserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import py4j.GatewayServer;

@Configuration
public class GatewayServerConfig {

    @Bean
    public GatewayServer gatewayServer(EntryPoint entryPoint){
        GatewayServer gatewayServer =  new GatewayServer(entryPoint);
        gatewayServer.start();
        return gatewayServer;
    }

}
