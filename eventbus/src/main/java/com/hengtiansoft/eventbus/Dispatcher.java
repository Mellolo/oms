package com.hengtiansoft.eventbus;

import com.google.common.collect.Queues;

import java.util.Iterator;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class Dispatcher {

    abstract void dispatch(BaseEvent event, Iterator<Subscriber> subscribers);

    static Dispatcher perThreadDispatchQueue() {
        return new PerThreadQueuedDispatcher();
    }

    private static final class PerThreadQueuedDispatcher extends Dispatcher {
        private final ThreadLocal<Queue<Event>> queue =
                new ThreadLocal<Queue<Event>>() {
                    @Override
                    protected Queue<Event> initialValue() {
                        return Queues.newArrayDeque();
                    }
                };

        /** Per-thread dispatch state, used to avoid reentrant event dispatching. */
        private final ThreadLocal<Boolean> dispatching =
                new ThreadLocal<Boolean>() {
                    @Override
                    protected Boolean initialValue() {
                        return false;
                    }
                };

        @Override
        void dispatch(BaseEvent event, Iterator<Subscriber> subscribers) {
            checkNotNull(event);
            checkNotNull(subscribers);
            Queue<Event> queueForThread = queue.get();
            queueForThread.offer(new Event(event, subscribers));

            if (!dispatching.get()) {
                dispatching.set(true);
                try {
                    Event nextEvent;
                    while ((nextEvent = queueForThread.poll()) != null) {
                        while (nextEvent.subscribers.hasNext()) {
                            nextEvent.subscribers.next().dispatchEvent(nextEvent.event);
                        }
                    }
                } finally {
                    dispatching.remove();
                    queue.remove();
                }
            }
        }

        private static final class Event {
            private final BaseEvent event;
            private final Iterator<Subscriber> subscribers;

            private Event(BaseEvent event, Iterator<Subscriber> subscribers) {
                this.event = event;
                this.subscribers = subscribers;
            }
        }
    }


}
