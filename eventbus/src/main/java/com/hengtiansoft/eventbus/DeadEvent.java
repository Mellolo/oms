package com.hengtiansoft.eventbus;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

public class DeadEvent extends BaseEvent {

    private final Object source;
    private final Object event;

    DeadEvent(Object source, BaseEvent event) {
        super(null);
        this.source = checkNotNull(source);
        this.event = checkNotNull(event);
    }

    public Object getSource() {
        return source;
    }

    public Object getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("source", source).add("event", event).toString();
    }
}
