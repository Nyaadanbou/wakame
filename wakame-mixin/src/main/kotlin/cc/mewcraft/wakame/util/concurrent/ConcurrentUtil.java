package cc.mewcraft.wakame.util.concurrent;

import cc.mewcraft.wakame.util.ExceptionLoggingScheduledThreadPoolExecutor;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@DefaultQualifier(NonNull.class)
public final class ConcurrentUtil {

    private ConcurrentUtil() {
    }

    public static void shutdownExecutor(final ExecutorService service, final TimeUnit timeoutUnit, final long timeoutLength) {
        service.shutdown();
        boolean didShutdown;
        try {
            didShutdown = service.awaitTermination(timeoutLength, timeoutUnit);
        } catch (final InterruptedException ignore) {
            didShutdown = false;
        }
        if (!didShutdown) {
            service.shutdownNow();
        }
    }

    public static ThreadFactory koishThreadFactory(final Logger logger, final String name) {
        return new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Koish " + name + " Thread #%d")
                .setUncaughtExceptionHandler((thread, thr) -> logger.warn("Uncaught exception on thread {}", thread.getName(), thr))
                .build();
    }

    public static ScheduledExecutorService createPeriodicTasksPool(final Logger logger) {
        return new ExceptionLoggingScheduledThreadPoolExecutor(
                1,
                koishThreadFactory(logger, "Periodic Tasks"),
                logger
        );
    }

}
