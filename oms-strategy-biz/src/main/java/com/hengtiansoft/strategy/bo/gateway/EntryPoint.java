package com.hengtiansoft.strategy.bo.gateway;

import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class EntryPoint {

    private Map<String, RunningStrategy> strategyMap;

    public RunningStrategy getStrategy(String id) {
        return strategyMap.get(id);
    }
}
