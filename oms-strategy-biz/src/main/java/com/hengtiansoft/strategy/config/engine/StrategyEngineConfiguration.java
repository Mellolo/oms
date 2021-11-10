package com.hengtiansoft.strategy.config.engine;

import com.hengtiansoft.eventbus.EventBus;
import com.hengtiansoft.strategy.bo.engine.StrategyEngine;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class StrategyEngineConfiguration {

    @Autowired
    ThreadPoolTaskExecutor poolTaskExecutor;

    @Bean
    public Map<String, RunningStrategy> strategyMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public EventBus eventBus()
    {
        return new EventBus();
    }

    @Bean
    public StrategyEngine strategyEngine()
    {
        return new StrategyEngine(strategyMap(), eventBus(), poolTaskExecutor);
    }
}
