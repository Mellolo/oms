package com.hengtiansoft.eventbus;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Throwables.throwIfUnchecked;

abstract class ExecutorPool
{
    abstract Executor executorOf(Subscriber subscriber);

    abstract public void shutdown();

    static ExecutorPool SingleThreadExecutorPool(String busIdentifier, int executorPoolNum) {
        return new SingleThreadExecutorPool(busIdentifier, executorPoolNum);
    }

    private static final class SingleThreadExecutorPool extends ExecutorPool {
        private final ExecutorService[] executorPool;
        private int executorPoolNum;

        SingleThreadExecutorPool(String busIdentifier, int executorPoolNum)
        {
            this.executorPoolNum = executorPoolNum;
            this.executorPool = new ExecutorService[executorPoolNum];
            for(int i=0;i<executorPoolNum;i++)
            {
                this.executorPool[i] = Executors.newSingleThreadExecutor(new EventBusThreadFactory(busIdentifier,i));
            }
        }

        private final LoadingCache<Subscriber, Integer> executorPoolNumForSubscriber =
                CacheBuilder.newBuilder()
                        .weakKeys()
                        .build(
                                new CacheLoader<Subscriber, Integer>() {
                                    @Override
                                    public Integer load(Subscriber subscriber) throws Exception {
                                        return getExecutorPoolNumForSubscriberNotCached(subscriber);
                                    }
                                });

        final Executor executorOf(Subscriber subscriber) {
            try {
                return executorPool[executorPoolNumForSubscriber.getUnchecked(subscriber)];
            } catch (UncheckedExecutionException e) {
                throwIfUnchecked(e.getCause());
                throw e;
            }
        }

        private int getExecutorPoolNumForSubscriberNotCached(Subscriber subscriber)
        {
            // TODO: 2021/4/27/027  改成更平均的方式
            return (int)(Math.random()*this.executorPoolNum);
        }

        public void shutdown()
        {
            for (int i=0;i<this.executorPoolNum;i++)
            {
                executorPool[i].shutdown();
            }
        }
    }

    private final static class EventBusThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final int priority;

        EventBusThreadFactory(String busIdentifier, int poolNum)
        {
            this(busIdentifier, poolNum, Thread.NORM_PRIORITY);
        }

        EventBusThreadFactory(String busIdentifier,int poolNum, int priority) {
            this.priority = priority;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = busIdentifier + "-pool-" + poolNum + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != priority)
                t.setPriority(priority);
            // TODO: 2021/4/27/027 异常处理还需要改改
            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace();
                }
            });
            return t;
        }
    }


}
