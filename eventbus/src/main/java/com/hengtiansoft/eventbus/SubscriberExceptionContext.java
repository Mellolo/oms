package com.hengtiansoft.eventbus;

import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

public class SubscriberExceptionContext {
    private final EventBus eventBus;
    private final BaseEvent event;
    private final BaseListener subscriber;
    private final Method subscriberMethod;

    SubscriberExceptionContext(
            EventBus eventBus, BaseEvent event, BaseListener subscriber, Method subscriberMethod) {
        this.eventBus = checkNotNull(eventBus);
        this.event = checkNotNull(event);
        this.subscriber = checkNotNull(subscriber);
        this.subscriberMethod = checkNotNull(subscriberMethod);
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public BaseEvent getEvent() {
        return event;
    }

    public BaseListener getSubscriber() {
        return subscriber;
    }

    public Method getSubscriberMethod() {
        return subscriberMethod;
    }
}