package com.hengtiansoft.strategy.bo.engine;

import com.hengtiansoft.eventbus.EventBus;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import com.hengtiansoft.strategy.exception.StrategyException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StrategyEngine {
    private Map<String, RunningStrategy> strategyMap;
    private EventBus eventBus;
    private Map<String, RunningStrategy> duplicateMap = new ConcurrentHashMap<>();

    public StrategyEngine(Map<String, RunningStrategy> strategyMap, EventBus eventBus) {
        this.strategyMap = strategyMap;
        this.eventBus = eventBus;
    }

    public void putStrategy(String strategyId, RunningStrategy runningStrategy) {
        strategyMap.put(strategyId, runningStrategy);
    }

    public RunningStrategy removeStrategy(String strategyId) {
        return strategyMap.remove(strategyId);
    }

    public void putDuplicate(String strategyId, RunningStrategy runningStrategy) {
        duplicateMap.put(strategyId, runningStrategy);
    }

    public void removeDuplicate(String strategyId) {
        duplicateMap.remove(strategyId);
    }

    public void registerStrategy(String strategyId) {
        RunningStrategy runningStrategy = strategyMap.get(strategyId);
        if(runningStrategy!=null) {
            strategyMap.get(strategyId).register(eventBus);
        }
        else {
            throw new StrategyException(strategyId, String.format("Cannot register strategyId: %s", strategyId));
        }
    }

    public void unregisterStrategy(String strategyId) {
        RunningStrategy runningStrategy = strategyMap.get(strategyId);
        if(runningStrategy!=null) {
            strategyMap.get(strategyId).unregister(eventBus);
        }
    }

    public boolean turnStrategy2Dupliacate(String strategyId) {
        RunningStrategy runningStrategy = strategyMap.get(strategyId);
        if(runningStrategy!=null) {
            runningStrategy.unregister(eventBus);
            strategyMap.remove(strategyId);
            duplicateMap.put(strategyId, runningStrategy);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean turnDupliacate2Strategy(String strategyId) {
        RunningStrategy runningStrategy = duplicateMap.get(strategyId);
        if(runningStrategy!=null) {
            duplicateMap.remove(strategyId);
            strategyMap.put(strategyId, runningStrategy);
            runningStrategy.register(eventBus);
            return true;
        }
        else {
            return false;
        }
    }
}
