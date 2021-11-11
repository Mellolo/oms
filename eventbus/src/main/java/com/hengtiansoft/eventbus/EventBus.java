package com.hengtiansoft.eventbus;

import com.google.common.base.MoreObjects;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class EventBus{
    private final String identifier;
    private final ExecutorPool executorPool;
    private final SubscriberExceptionHandler exceptionHandler;
    private final SubscriberRegistry subscribers = new SubscriberRegistry(this);
    private final Dispatcher dispatcher;

    private boolean postable = true;

    public EventBus() {
        this("default",4);
    }

    public EventBus(String identifier) {
        this(identifier, 4);
    }

    public EventBus(int executorPoolNum) {
        this("default", executorPoolNum);
    }

    public EventBus(String identifier, int executorPoolNum) {
        this(
                identifier,
                ExecutorPool.SingleThreadExecutorPool(identifier, executorPoolNum),
                Dispatcher.perThreadDispatchQueue(),
                LoggingHandler.INSTANCE);
    }

    EventBus(
            String identifier,
            ExecutorPool executorPool,
            Dispatcher dispatcher,
            SubscriberExceptionHandler exceptionHandler) {
        this.identifier = checkNotNull(identifier);
        this.dispatcher = checkNotNull(dispatcher);
        this.exceptionHandler = checkNotNull(exceptionHandler);
        this.executorPool = executorPool;
    }

    public final String identifier() {
        return identifier;
    }

    final Executor executorOf(Subscriber subscriber) {
        return executorPool.executorOf(subscriber);
    }

    void handleSubscriberException(Throwable e, SubscriberExceptionContext context) {
        checkNotNull(e);
        checkNotNull(context);
        try {
            exceptionHandler.handleException(e, context);
        } catch (Throwable e2) {
            log.error(String.format(Locale.ROOT, "Exception %s thrown while handling exception: %s", e2, e), e2);
        }
    }

    public void clear() {
        subscribers.clear();
    }

    void register(BaseListener listener) {
        subscribers.register(listener);
    }

    void unregister(BaseListener listener) {
        try {
            subscribers.unregister(listener);
        } catch (Exception e) {
            log.warn(String.format("Unregister Exception: %s", e), e);
        }
    }

    public void subscribe(BaseListener listener, Class<?> clazz, String tag)
    {
        subscribers.subscribe(listener, clazz, new LinkedList<String>(){ {add(tag);} });
    }

    public void subscribe(BaseListener listener, Class<?> clazz, Iterable<String> tags)
    {
        subscribers.subscribe(listener, clazz, tags);
    }

    public void unsubscribe(BaseListener listener, Class<?> clazz, String tag)
    {
        subscribers.unsubscribe(listener, clazz, new LinkedList<String>(){ {add(tag);} });
    }

    public void unsubscribe(BaseListener listener, Class<?> clazz, Iterable<String> tags)
    {
        subscribers.unsubscribe(listener, clazz, tags);
    }

    public void post(BaseEvent event) {
        if(postable) {
            Iterator<Subscriber> eventSubscribers = subscribers.getSubscribers(event);
            if (eventSubscribers.hasNext()) {
                dispatcher.dispatch(event, eventSubscribers);
            } else if (!(event instanceof DeadEvent)) {
                post(new DeadEvent(this, event));
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(identifier).toString();
    }

    public void shutdown()
    {
        postable = false;
        executorPool.shutdown();
    }

    @Slf4j
    static final class LoggingHandler implements SubscriberExceptionHandler {
        static final LoggingHandler INSTANCE = new LoggingHandler();

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            log.error(
                    String.format("%s.%s: %s", EventBus.class.getName(), context.getEventBus().identifier(), exception),
                    exception
            );
        }

        private static String message(SubscriberExceptionContext context) {
            Method method = context.getSubscriberMethod();
            return "Exception thrown by subscriber method "
                    + method.getName()
                    + '('
                    + method.getParameterTypes()[0].getName()
                    + ')'
                    + " on subscriber "
                    + context.getSubscriber()
                    + " when dispatching event: "
                    + context.getEvent();
        }
    }

}