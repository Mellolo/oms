package com.hengtiansoft.eventbus;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.*;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.j2objc.annotations.Weak;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.throwIfUnchecked;

final class SubscriberRegistry {
    private static Iterator<Subscriber> EmptyIterator = null;

    private final ConcurrentMap<EventIdentifier, CopyOnWriteArraySet<Subscriber>> subscribers =
            Maps.newConcurrentMap();

    @Weak
    private final EventBus bus;

    private static Iterator<Subscriber> getEmptyIterator()
    {
        if(EmptyIterator==null)
        {
            synchronized (SubscriberRegistry.class)
            {
                if(EmptyIterator==null)
                {
                    EmptyIterator = new ArrayList<Subscriber>(0).iterator();
                }
            }
        }
        return EmptyIterator;
    }

    SubscriberRegistry(EventBus bus) {
        this.bus = checkNotNull(bus);
    }

    void clear() {
        subscribers.clear();
    }

    void subscribe(BaseListener listener, Class<?> clazz, Iterable<String> tags)
    {
        Set<Subscriber> listenerMethodsForClazz = findSubscribersOfClass(listener,clazz);
        for(String tag:tags) {
            EventIdentifier eventType = EventIdentifier.getEventIdentifier(clazz, tag);
            Set<Subscriber> eventSubscribers = getSubscriberSet(eventType);
            eventSubscribers.addAll(listenerMethodsForClazz);
        }
    }

    void unsubscribe(BaseListener listener, Class<?> clazz, Iterable<String> tags)
    {
        Set<Subscriber> listenerMethodsForClazz = findSubscribersOfClass(listener,clazz);
        for(String tag:tags) {
            EventIdentifier eventType = EventIdentifier.getEventIdentifier(clazz,tag);
            CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);
            if (eventSubscribers == null || !eventSubscribers.removeAll(listenerMethodsForClazz)) {
                throw new IllegalArgumentException(
                        "missing event subscriber for an annotated method. Is " + listener + " registered?");
            }
        }
    }

    void register(BaseListener listener) {
        Multimap<EventIdentifier, Subscriber> listenerMethods = findAllSubscribers(listener);

        for (Map.Entry<EventIdentifier, Collection<Subscriber>> entry : listenerMethods.asMap().entrySet()) {
            EventIdentifier eventType = entry.getKey();
            Collection<Subscriber> eventMethodsInListener = entry.getValue();

            Set<Subscriber> eventSubscribers = getSubscriberSet(eventType);
            eventSubscribers.addAll(eventMethodsInListener);
        }
    }

    void unregister(BaseListener listener) {
        Multimap<EventIdentifier, Subscriber> listenerMethods = findAllSubscribers(listener);

        for (Map.Entry<EventIdentifier, Collection<Subscriber>> entry : listenerMethods.asMap().entrySet()) {
            EventIdentifier eventType = entry.getKey();
            Collection<Subscriber> listenerMethodsForType = entry.getValue();

            CopyOnWriteArraySet<Subscriber> currentSubscribers = subscribers.get(eventType);
            if (currentSubscribers == null || !currentSubscribers.removeAll(listenerMethodsForType)) {
                // if removeAll returns true, all we really know is that at least one subscriber was
                // removed... however, barring something very strange we can assume that if at least one
                // subscriber was removed, all subscribers on listener for that event type were... after
                // all, the definition of subscribers on a particular class is totally static
                throw new IllegalArgumentException(
                        "missing event subscriber for an annotated method. Is " + listener + " registered?");
            }
        }
    }

    private Set<Subscriber> getSubscriberSet(EventIdentifier eventType) {
        CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);
        if (eventSubscribers == null) {
            CopyOnWriteArraySet<Subscriber> newSet = new CopyOnWriteArraySet<>();
            eventSubscribers = MoreObjects.firstNonNull(subscribers.putIfAbsent(eventType, newSet), newSet);
        }
        return eventSubscribers;
    }

    Iterator<Subscriber> getSubscribers(BaseEvent event) {
        EventIdentifier eventType = event.getEventIdentifier();
        CopyOnWriteArraySet<Subscriber> eventSubscribers = subscribers.get(eventType);
        return (eventSubscribers!=null)?eventSubscribers.iterator():getEmptyIterator();
    }

    private Set<Subscriber> findSubscribersOfClass(BaseListener listener, Class<?> eventClass) {
        Set<Subscriber> methodsInListener = new HashSet<>();
        Class<?> clazz = listener.getClass();
        for (Method method : getAnnotatedMethods(clazz)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> eventType = parameterTypes[0];
            if(eventType.equals(eventClass)) {
                methodsInListener.add(Subscriber.create(bus, listener, method));
            }
        }
        return methodsInListener;
    }

    private Multimap<EventIdentifier, Subscriber> findAllSubscribers(BaseListener listener) {
        Multimap<EventIdentifier, Subscriber> methodsInListener = HashMultimap.create();
        Class<?> clazz = listener.getClass();
        for (Method method : getAnnotatedMethods(clazz)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> eventType = parameterTypes[0];
            Iterable<EventIdentifier> eventIdentifiers = listener.getEventIdentifiers(eventType);
            Subscriber subscriber = Subscriber.create(bus, listener, method);
            for(EventIdentifier eventIdentifier: eventIdentifiers)
                methodsInListener.put(eventIdentifier, subscriber);
        }
        return methodsInListener;
    }

    /**
     * A thread-safe cache that contains the mapping from each class to all methods in that class and
     * all super-classes, that are annotated with {@code @Subscribe}. The cache is shared across all
     * instances of this class; this greatly improves performance if multiple EventBus instances are
     * created and objects of the same class are registered on all of them.
     */
    private static final LoadingCache<Class<?>, ImmutableList<Method>> subscriberMethodsCache =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .build(
                            new CacheLoader<Class<?>, ImmutableList<Method>>() {
                                @Override
                                public ImmutableList<Method> load(Class<?> concreteClass) throws Exception {
                                    return getAnnotatedMethodsNotCached(concreteClass);
                                }
                            });

    private static ImmutableList<Method> getAnnotatedMethods(Class<?> clazz) {
        try {
            return subscriberMethodsCache.getUnchecked(clazz);
        } catch (UncheckedExecutionException e) {
            throwIfUnchecked(e.getCause());
            throw e;
        }
    }

    private static ImmutableList<Method> getAnnotatedMethodsNotCached(Class<?> clazz) {
        Set<? extends Class<?>> supertypes = TypeToken.of(clazz).getTypes().rawTypes();
        Map<MethodIdentifier, Method> identifiers = Maps.newHashMap();
        for (Class<?> supertype : supertypes) {
            for (Method method : supertype.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SubscribeEvent.class) && !method.isSynthetic()) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    checkArgument(
                            parameterTypes.length == 1,
                            "Method %s has @Subscribe annotation but has %s parameters. "
                                    + "Subscriber methods must have exactly 1 parameter.",
                            method,
                            parameterTypes.length);

                    checkArgument(
                            BaseEvent.class.isAssignableFrom(parameterTypes[0]),
                            "@Subscribe method %s's parameter is %s. "
                                    + "Subscriber methods cannot accept none-BaseEvent. "
                                    + "Consider changing the parameter to %s.",
                            method,
                            parameterTypes[0].getName(),
                            Primitives.wrap(parameterTypes[0]).getSimpleName());

                    MethodIdentifier ident = new MethodIdentifier(method);
                    if (!identifiers.containsKey(ident)) {
                        identifiers.put(ident, method);
                    }
                }
            }
        }
        return ImmutableList.copyOf(identifiers.values());
    }

    private static final class MethodIdentifier {

        private final String name;
        private final List<Class<?>> parameterTypes;

        MethodIdentifier(Method method) {
            this.name = method.getName();
            this.parameterTypes = Arrays.asList(method.getParameterTypes());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, parameterTypes);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof MethodIdentifier) {
                MethodIdentifier ident = (MethodIdentifier) o;
                return name.equals(ident.name) && parameterTypes.equals(ident.parameterTypes);
            }
            return false;
        }
    }
}
