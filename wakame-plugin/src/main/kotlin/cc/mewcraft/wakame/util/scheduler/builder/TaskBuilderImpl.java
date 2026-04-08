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

package cc.mewcraft.wakame.util.scheduler.builder;

import cc.mewcraft.wakame.util.Schedulers;
import cc.mewcraft.wakame.util.promise.Promise;
import cc.mewcraft.wakame.util.promise.ThreadContext;
import cc.mewcraft.wakame.util.scheduler.Task;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NullMarked
class TaskBuilderImpl implements TaskBuilder {
    static final TaskBuilder INSTANCE = new TaskBuilderImpl();

    private final ThreadContextual sync;
    private final ThreadContextual async;

    private TaskBuilderImpl() {
        this.sync = new ThreadContextualBuilder(ThreadContext.SYNC);
        this.async = new ThreadContextualBuilder(ThreadContext.ASYNC);
    }

    @Override
    public ThreadContextual sync() {
        return this.sync;
    }

    @Override
    public ThreadContextual async() {
        return this.async;
    }

    private static final class ThreadContextualBuilder implements ThreadContextual {
        private final ThreadContext context;
        private final ContextualPromiseBuilder instant;

        ThreadContextualBuilder(ThreadContext context) {
            this.context = context;
            this.instant = new ContextualPromiseBuilderImpl(context);
        }

        @Override
        public ContextualPromiseBuilder now() {
            return this.instant;
        }

        @Override
        public DelayedTick after(long ticks) {
            return new DelayedTickBuilder(this.context, ticks);
        }

        @Override
        public DelayedTime after(long duration, TimeUnit unit) {
            return new DelayedTimeBuilder(this.context, duration, unit);
        }

        @Override
        public ContextualTaskBuilder afterAndEvery(long ticks) {
            return new ContextualTaskBuilderTickImpl(this.context, ticks, ticks);
        }

        @Override
        public ContextualTaskBuilder afterAndEvery(long duration, TimeUnit unit) {
            return new ContextualTaskBuilderTimeImpl(this.context, duration, unit, duration, unit);
        }

        @Override
        public ContextualTaskBuilder every(long ticks) {
            return new ContextualTaskBuilderTickImpl(this.context, 0, ticks);
        }

        @Override
        public ContextualTaskBuilder every(long duration, TimeUnit unit) {
            return new ContextualTaskBuilderTimeImpl(this.context, 0, TimeUnit.NANOSECONDS, duration, unit);
        }
    }

    private static final class DelayedTickBuilder implements DelayedTick {
        private final ThreadContext context;
        private final long delay;

        DelayedTickBuilder(ThreadContext context, long delay) {
            this.context = context;
            this.delay = delay;
        }

        @Override
        public <T> Promise<T> supply(Supplier<T> supplier) {
            return Schedulers.get(this.context).supplyLater(supplier, this.delay);
        }

        @Override
        public <T> Promise<T> call(Callable<T> callable) {
            return Schedulers.get(this.context).callLater(callable, this.delay);
        }

        @Override
        public Promise<Void> run(Runnable runnable) {
            return Schedulers.get(this.context).runLater(runnable, this.delay);
        }

        @Override
        public ContextualTaskBuilder every(long ticks) {
            return new ContextualTaskBuilderTickImpl(this.context, this.delay, ticks);
        }
    }

    private static final class DelayedTimeBuilder implements DelayedTime {
        private final ThreadContext context;
        private final long delay;
        private final TimeUnit delayUnit;

        DelayedTimeBuilder(ThreadContext context, long delay, TimeUnit delayUnit) {
            this.context = context;
            this.delay = delay;
            this.delayUnit = delayUnit;
        }

        @Override
        public <T> Promise<T> supply(Supplier<T> supplier) {
            return Schedulers.get(this.context).supplyLater(supplier, this.delay, this.delayUnit);
        }

        @Override
        public <T> Promise<T> call(Callable<T> callable) {
            return Schedulers.get(this.context).callLater(callable, this.delay, this.delayUnit);
        }

        @Override
        public Promise<Void> run(Runnable runnable) {
            return Schedulers.get(this.context).runLater(runnable, this.delay, this.delayUnit);
        }

        @Override
        public ContextualTaskBuilder every(long duration, TimeUnit unit) {
            return new ContextualTaskBuilderTimeImpl(this.context, this.delay, this.delayUnit, duration, unit);
        }
    }

    private static class ContextualPromiseBuilderImpl implements ContextualPromiseBuilder {
        private final ThreadContext context;

        ContextualPromiseBuilderImpl(ThreadContext context) {
            this.context = context;
        }

        @Override
        public <T> Promise<T> supply(Supplier<T> supplier) {
            return Schedulers.get(this.context).supply(supplier);
        }

        @Override
        public <T> Promise<T> call(Callable<T> callable) {
            return Schedulers.get(this.context).call(callable);
        }

        @Override
        public Promise<Void> run(Runnable runnable) {
            return Schedulers.get(this.context).run(runnable);
        }
    }

    private static class ContextualTaskBuilderTickImpl implements ContextualTaskBuilder {
        private final ThreadContext context;
        private final long delay;
        private final long interval;

        ContextualTaskBuilderTickImpl(ThreadContext context, long delay, long interval) {
            this.context = context;
            this.delay = delay;
            this.interval = interval;
        }

        @Override
        public Task consume(Consumer<Task> consumer) {
            return Schedulers.get(this.context).runRepeating(consumer, this.delay, this.interval);
        }

        @Override
        public Task run(Runnable runnable) {
            return Schedulers.get(this.context).runRepeating(runnable, this.delay, this.interval);
        }
    }

    private static class ContextualTaskBuilderTimeImpl implements ContextualTaskBuilder {
        private final ThreadContext context;
        private final long delay;
        private final TimeUnit delayUnit;
        private final long interval;
        private final TimeUnit intervalUnit;

        ContextualTaskBuilderTimeImpl(ThreadContext context, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit) {
            this.context = context;
            this.delay = delay;
            this.delayUnit = delayUnit;
            this.interval = interval;
            this.intervalUnit = intervalUnit;
        }

        @Override
        public Task consume(Consumer<Task> consumer) {
            return Schedulers.get(this.context).runRepeating(consumer, this.delay, this.delayUnit, this.interval, this.intervalUnit);
        }

        @Override
        public Task run(Runnable runnable) {
            return Schedulers.get(this.context).runRepeating(runnable, this.delay, this.delayUnit, this.interval, this.intervalUnit);
        }
    }
}
