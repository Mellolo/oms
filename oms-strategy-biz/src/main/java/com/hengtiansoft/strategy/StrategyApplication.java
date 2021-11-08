package com.hengtiansoft.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.hengtiansoft.strategy.feign")
public class StrategyApplication {

    public static void main(String[] args) {
        try {
            Thread.sleep(3000);
            SpringApplication.run(StrategyApplication.class, args);
        } catch (Exception e) {
            log.error("Main Error Thread.sleep");
        }
    }

}

