package com.hengtiansoft.eventbus;

import com.google.j2objc.annotations.Weak;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

class Subscriber
{
    static Subscriber create(EventBus bus, BaseListener listener, Method method) {
        return isDeclaredThreadSafe(method)
                ? new Subscriber(bus, listener, method)
                : new SynchronizedSubscriber(bus, listener, method);
    }
    @Weak
    private EventBus bus;
    private final BaseListener target;
    private final Method method;
    private final Executor executor;

    private Subscriber(EventBus bus, BaseListener target, Method method) {
        this.bus = bus;
        this.target = checkNotNull(target);
        this.method = method;
        method.setAccessible(true);

        this.executor = bus.executorOf(this);
    }

    final void dispatchEvent(final BaseEvent event) {
        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            invokeSubscriberMethod(event);
                        } catch (InvocationTargetException e) {
                            bus.handleSubscriberException(e.getCause(), context(event));
                        }
                    }
                });
    }

    private SubscriberExceptionContext context(BaseEvent event) {
        return new SubscriberExceptionContext(bus, event, target, method);
    }

    void invokeSubscriberMethod(BaseEvent event) throws InvocationTargetException {
        try {
            method.invoke(target, checkNotNull(event));
        } catch (IllegalArgumentException e) {
            throw new Error("Method rejected target/argument: " + event, e);
        } catch (IllegalAccessException e) {
            throw new Error("Method became inaccessible: " + event, e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    @Override
    public final int hashCode() {
        return (31 + method.hashCode()) * 31 + System.identityHashCode(target);
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        if (obj instanceof Subscriber) {
            Subscriber that = (Subscriber) obj;
            return target == that.target && method.equals(that.method);
        }
        return false;
    }

    private static boolean isDeclaredThreadSafe(Method method) {
        return method.getAnnotation(DisallowConcurrentEvents.class) == null;
    }

    static final class SynchronizedSubscriber extends Subscriber {

        private SynchronizedSubscriber(EventBus bus, BaseListener target, Method method) {
            super(bus, target, method);
        }

        @Override
        void invokeSubscriberMethod(BaseEvent event) throws InvocationTargetException {
            synchronized (this) {
                super.invokeSubscriberMethod(event);
            }
        }
    }

}
