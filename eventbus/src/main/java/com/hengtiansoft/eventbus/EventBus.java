package com.hengtiansoft.eventbus;

import com.google.common.base.MoreObjects;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class EventBus{
    private static final Logger logger = Logger.getLogger(EventBus.class.getName());

    private final String identifier;
    private final ExecutorPool executorPool;
    private final SubscriberExceptionHandler exceptionHandler;
    private final SubscriberRegistry subscribers = new SubscriberRegistry(this);
    private final Dispatcher dispatcher;

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
            // if the handler threw an exception... well, just log it
            logger.log(
                    Level.SEVERE,
                    String.format(Locale.ROOT, "Exception %s thrown while handling exception: %s", e2, e),
                    e2);
        }
    }

    public void register(BaseListener listener) {
        subscribers.register(listener);
    }

    public void unregister(BaseListener listener) {
        subscribers.unregister(listener);
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
        Iterator<Subscriber> eventSubscribers = subscribers.getSubscribers(event);
        if (eventSubscribers.hasNext()) {
            dispatcher.dispatch(event, eventSubscribers);
        } else if (!(event instanceof DeadEvent)) {
            // the event had no subscribers and was not itself a DeadEvent
            post(new DeadEvent(this, event));
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).addValue(identifier).toString();
    }

    public void shutdown()
    {
        // TODO: 2021/4/28/028 增加其他收尾内容
        executorPool.shutdown();
    }

    static final class LoggingHandler implements SubscriberExceptionHandler {
        static final LoggingHandler INSTANCE = new LoggingHandler();

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            Logger logger = logger(context);
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, message(context), exception);
            }
        }

        private static Logger logger(SubscriberExceptionContext context) {
            return Logger.getLogger(EventBus.class.getName() + "." + context.getEventBus().identifier());
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