package com.hengtiansoft.strategy.bo.engine;

import com.hengtiansoft.eventbus.EventBus;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class StrategyEngine {
    private Map<String, RunningStrategy> strategyMap;
    private EventBus eventBus;
}
