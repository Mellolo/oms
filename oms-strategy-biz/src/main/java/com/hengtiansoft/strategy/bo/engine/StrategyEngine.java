package com.hengtiansoft.strategy.bo.engine;

import com.hengtiansoft.eventbus.BaseEvent;
import com.hengtiansoft.eventbus.EventBus;
import com.hengtiansoft.strategy.bo.strategy.RunningStrategy;
import com.hengtiansoft.strategy.exception.StrategyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class StrategyEngine {
    private ThreadPoolTaskExecutor poolTaskExecutor;

    private Map<String, RunningStrategy> strategyMap;
    private EventBus eventBus;
    private Map<String, RunningStrategy> duplicateMap = new ConcurrentHashMap<>();
    private Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    public StrategyEngine(Map<String, RunningStrategy> strategyMap, EventBus eventBus, ThreadPoolTaskExecutor poolTaskExecutor) {
        this.strategyMap = strategyMap;
        this.eventBus = eventBus;
        this.poolTaskExecutor = poolTaskExecutor;
    }

    public void post(BaseEvent event) {
        eventBus.post(event);
    }

    public void clear() {
        eventBus.clear();
        clearMapStrategy(strategyMap);
        clearMapStrategy(duplicateMap);
        lockMap.clear();
    }

    private void clearMapStrategy(Map<String, RunningStrategy> map) {
        for(Map.Entry<String, RunningStrategy> entry: map.entrySet()) {
            poolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    entry.getValue().destroy();
                }
            });
        }
        map.clear();
    }

    public boolean contains(String strategyId) {
        return strategyMap.containsKey(strategyId) || duplicateMap.containsKey(strategyId);
    }

    public boolean containsStrategy(String strategyId) {
        return strategyMap.containsKey(strategyId);
    }

    public boolean containsDuplicate(String strategyId) {
        return duplicateMap.containsKey(strategyId);
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

    public RunningStrategy removeDuplicate(String strategyId) {
        return duplicateMap.remove(strategyId);
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

    public void turnStrategy2Duplicate(String strategyId) {
        RunningStrategy runningStrategy = strategyMap.get(strategyId);
        if(runningStrategy!=null) {
            runningStrategy.unregister(eventBus);
            strategyMap.remove(strategyId);
            duplicateMap.put(strategyId, runningStrategy);
        }
    }

    public void turnDuplicate2Strategy(String strategyId) {
        RunningStrategy runningStrategy = duplicateMap.get(strategyId);
        if(runningStrategy!=null) {
            duplicateMap.remove(strategyId);
            strategyMap.put(strategyId, runningStrategy);
            runningStrategy.register(eventBus);
        }
        else {
            throw new StrategyException(strategyId, "Error turnDuplicate2Strategy");
        }
    }

    public ReentrantLock getLock(String strategyId) {
        if(!lockMap.containsKey(strategyId)) {
            lockMap.computeIfAbsent(strategyId, k->new ReentrantLock());
        }
        return lockMap.get(strategyId);
    }

    public void deleteLock(String strategyId) {
        lockMap.remove(strategyId);
    }
}
