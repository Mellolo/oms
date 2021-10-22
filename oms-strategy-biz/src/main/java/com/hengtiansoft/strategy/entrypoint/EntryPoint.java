package com.hengtiansoft.strategy.entrypoint;

import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class EntryPoint {

    private Map<String, RunningStrategy> strategyMap;

    public String matchTest(String a, String b)
    {
        return a + b;
    }

    public RunningStrategy getStrategy(String id) {
        return strategyMap.get(id);
    }
}
