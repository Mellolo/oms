package com.hengtiansoft.strategy.bo.strategy;

import com.hengtiansoft.eventbus.SubscribeEvent;
import com.hengtiansoft.strategy.model.Strategy;
import com.hengtiansoft.strategy.bo.event.TickEvent;


public class RunningStrategy extends BaseStrategy {

    private String id;
    private Strategy strategy;

    public RunningStrategy(String id, Strategy strategy)
    {
        this.id = id;
        this.strategy = strategy;
    }

    public void init()
    {
    }

    //@SubscribeEvent
    //@DisallowConcurrentEvents
    public void HandleTickSynchronized(TickEvent tickEvent) throws Exception
    {
    }

    final public void subscribe(String security)
    {
        addEventListened(TickEvent.class, security);
    }

    @SubscribeEvent
    public void HandleTick(TickEvent tickEvent)
    {
    }
}
