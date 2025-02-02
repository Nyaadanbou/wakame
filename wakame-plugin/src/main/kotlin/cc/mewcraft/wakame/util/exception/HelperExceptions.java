/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package cc.mewcraft.wakame.util.exception;

import cc.mewcraft.wakame.KoishBootstrapperKt;
import cc.mewcraft.wakame.util.event.Events;
import cc.mewcraft.wakame.util.exception.events.HelperExceptionEvent;
import cc.mewcraft.wakame.util.exception.types.EventHandlerException;
import cc.mewcraft.wakame.util.exception.types.PromiseChainException;
import cc.mewcraft.wakame.util.exception.types.SchedulerTaskException;
import cc.mewcraft.wakame.util.interfaces.Delegate;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central handler for exceptions that occur within user-written
 * Runnables and handlers running in helper.
 */
public final class HelperExceptions {
    private HelperExceptions() {
    }

    private static final ThreadLocal<AtomicBoolean> NOT_TODAY_STACK_OVERFLOW_EXCEPTION =
            ThreadLocal.withInitial(() -> new AtomicBoolean(false));

    private static void log(InternalException exception) {
        // print to logger
        KoishBootstrapperKt.getLogger().error(exception.getMessage(), exception);

        // call event
        AtomicBoolean firing = NOT_TODAY_STACK_OVERFLOW_EXCEPTION.get();
        if (firing.compareAndSet(false, true)) {
            try {
                Events.call(new HelperExceptionEvent(exception));
            } finally {
                firing.set(false);
            }
        }
    }

    public static void reportScheduler(Throwable throwable) {
        log(new SchedulerTaskException(throwable));
    }

    public static void reportPromise(Throwable throwable) {
        log(new PromiseChainException(throwable));
    }

    public static void reportEvent(Object event, Throwable throwable) {
        log(new EventHandlerException(throwable, event));
    }

    public static Runnable wrapSchedulerTask(Runnable runnable) {
        return new SchedulerWrappedRunnable(runnable);
    }

    private static final class SchedulerWrappedRunnable implements Runnable, Delegate<Runnable> {
        private final Runnable delegate;

        private SchedulerWrappedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                this.delegate.run();
            } catch (Throwable t) {
                reportScheduler(t);
            }
        }

        @Override
        public Runnable getDelegate() {
            return this.delegate;
        }
    }

}
