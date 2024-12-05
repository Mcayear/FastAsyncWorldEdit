package com.boydti.fawe.cloudburst.adapter;

import co.aikar.timings.Timings;
import com.fastasyncworldedit.core.queue.implementation.QueueHandler;
import com.boydti.fawe.cloudburst.listener.ChunkListener;

import java.lang.reflect.Method;

import static org.slf4j.LoggerFactory.getLogger;

public class CloudburstQueueHandler extends QueueHandler {
    private volatile boolean timingsEnabled;
    private static boolean alertTimingsChange = true;

    private static Method methodCheck;

    static {
        try {
            methodCheck = Class.forName("co.aikar.timings.TimingsManager").getDeclaredMethod("recheckEnabled");
            methodCheck.setAccessible(true);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Indicate an unsafe task is starting. Physics are frozen, async catchers disabled, etc. for the duration of the task
     *
     * @param parallel If the task is being run async and/or in parallel
     */
    @Override
    public void startUnsafe(final boolean parallel) {
        ChunkListener.physicsFreeze = true;
        if (parallel) {
            try {
                timingsEnabled = Timings.isTimingsEnabled();
                if (timingsEnabled) {
                    if (alertTimingsChange) {
                        alertTimingsChange = false;
                        getLogger(CloudburstQueueHandler.class).debug("Having `parallel-threads` > 1 interferes with the timings.");
                    }
                    Timings.setTimingsEnabled(false);
                    methodCheck.invoke(null);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Indicate a/the unsafe task submitted after a {@link QueueHandler#startUnsafe(boolean)} call has ended.
     *
     * @param parallel If the task was being run async and/or in parallel
     */
    @Override
    public void endUnsafe(final boolean parallel) {
        ChunkListener.physicsFreeze = false;
        if (parallel) {
            try {
                if (timingsEnabled) {
                    Timings.setTimingsEnabled(true);
                    methodCheck.invoke(null);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

}
