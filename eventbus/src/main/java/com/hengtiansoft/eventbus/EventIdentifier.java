package com.hengtiansoft.eventbus;

import java.util.Objects;

final class EventIdentifier {
    private final Class<?> clazz;
    private final String tag;

    static EventIdentifier getEventIdentifier(Class<?> clazz, String tag)
    {
        //可以尝试改成常量池形式
        return new EventIdentifier(clazz,tag);
    }

    private EventIdentifier(Class<?> clazz, String tag) {
        this.clazz = clazz;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventIdentifier that = (EventIdentifier) o;
        return Objects.equals(clazz, that.clazz) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, tag);
    }

    @Override
    public String toString() {
        return "EventIdentifier{" +
                "clazz=" + clazz +
                ", tag='" + tag + '\'' +
                '}';
    }
}
