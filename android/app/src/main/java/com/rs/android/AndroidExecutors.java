package com.rs.android;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Stand-ins for JDK executor factories Android does not have.
 *
 * rs.darkan:core is compiled for Java 24 and asks for virtual threads in a few
 * places (ServerChannelHandler, DBItemManager). Android has no virtual threads
 * at any API level, so those calls fail at class-load time with a
 * NoSuchMethodError. The core jar is rewritten for Android anyway (see
 * downgradeDarkanCore), and that rewrite repoints the calls here.
 *
 * Signatures must match the JDK methods they stand in for exactly, since the
 * rewrite only changes which class the call targets.
 */
public final class AndroidExecutors {

    private static final AtomicLong THREAD_COUNT = new AtomicLong();

    private AndroidExecutors() {}

    /**
     * Stands in for Executors.newVirtualThreadPerTaskExecutor(). Virtual threads
     * are cheap and unbounded; a cached pool is the closest platform-thread
     * equivalent, reusing idle threads instead of creating one per task.
     */
    public static ExecutorService newVirtualThreadPerTaskExecutor() {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, "vthread-" + THREAD_COUNT.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
    }
}
