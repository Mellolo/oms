package com.hengtiansoft.strategy.config;

import com.hengtiansoft.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventBusConfig {
    @Bean
    public EventBus eventBus()
    {
        return new EventBus();
    }
}
