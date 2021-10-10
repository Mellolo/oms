package com.hengtiansoft.eventbus;

import java.util.Objects;

public abstract class BaseEvent {
    private final String tag;

    public BaseEvent(String tag){
        this.tag = tag;
    }

    final public EventIdentifier getEventIdentifier()
    {
        return EventIdentifier.getEventIdentifier(this.getClass(),tag);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEvent event = (BaseEvent) o;
        return Objects.equals(tag, event.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "tag='" + tag + '\'' +
                '}';
    }
}


