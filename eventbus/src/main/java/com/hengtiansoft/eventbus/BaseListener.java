package com.hengtiansoft.eventbus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

public abstract class BaseListener {
    private Set<EventBus> busSet = new HashSet<>();
    private final Multimap<Class<?>, String> eventTags = HashMultimap.create();

    public final void addEventListened(Class<?> clazz, String tag)
    {
        if(eventTags.containsEntry(clazz,tag)) return;
        eventTags.put(clazz, tag);
        for(EventBus bus: busSet)
        {
            bus.subscribe(this, clazz, tag);
        }
    }

    public final void removeEventListened(Class<?> clazz, String tag)
    {
        if(!eventTags.containsEntry(clazz,tag)) return;
        eventTags.remove(clazz, tag);
        for(EventBus bus: busSet)
        {
            bus.unsubscribe(this, clazz, tag);
        }
    }

    public final void addAllEventListened(Class<?> clazz, Iterable<String> tags)
    {
        List<String> tagsToAdd = new ArrayList<>();
        for(String tag:tags)
            if(!eventTags.containsEntry(clazz, tag))
                tagsToAdd.add(tag);
        eventTags.putAll(clazz, tagsToAdd);
        for(EventBus bus: busSet)
        {
            bus.subscribe(this, clazz, tagsToAdd);
        }
    }

    public final void removeAllEventListened(Class<?> clazz)
    {
        Collection<String> tagsToRemove = eventTags.removeAll(clazz);
        for(EventBus bus: busSet)
        {
            bus.unsubscribe(this, clazz, tagsToRemove);
        }
    }

    public final Iterable<EventIdentifier> getEventIdentifiers(Class<?> clazz)
    {
        List<EventIdentifier> eventIdentifierList = new ArrayList<>();
        for(String tag: eventTags.get(clazz))
        {
            eventIdentifierList.add(EventIdentifier.getEventIdentifier(clazz,tag));
        }
        return eventIdentifierList;
    }

    public void register(EventBus eventBus)
    {
        busSet.add(eventBus);
        eventBus.register(this);
    }

    public void unregister(EventBus eventBus)
    {
        busSet.remove(eventBus);
        eventBus.unregister(this);
    }

    protected void unregisterAll()
    {
        for(EventBus bus: busSet)
        {
            this.unregister(bus);
        }
    }

}
