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

package cc.mewcraft.wakame.util.promise;

import cc.mewcraft.wakame.util.Delegates;
import cc.mewcraft.wakame.util.terminable.Terminable;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An object that acts as a proxy for a result that is initially unknown,
 * usually because the computation of its value is yet incomplete.
 *
 * <p>This interface carries similar method signatures to those of
 * {@link java.util.concurrent.CompletionStage} and {@link CompletableFuture}.</p>
 *
 * <p>However, a distinction is made between actions which are executed on
 * the main server thread vs asynchronously.</p>
 *
 * @param <V> the result type
 */
@NullMarked
public interface Promise<V> extends Future<V>, Terminable {

    /**
     * Returns a new empty Promise
     *
     * <p>An empty promise can be 'completed' via the supply methods.</p>
     *
     * @param <U> the result type
     * @return a new empty promise
     */
    static <U> Promise<U> empty() {
        return HelperPromise.empty();
    }

    /**
     * Returns a new base promise to be built on top of.
     *
     * @return a new promise
     */
    static Promise<Void> start() {
        return HelperPromise.completed(null);
    }

    /**
     * Returns a Promise which is already completed with the given value.
     *
     * @param value the value
     * @param <U>   the result type
     * @return a new completed promise
     */
    static <U> Promise<U> completed(@Nullable U value) {
        return HelperPromise.completed(value);
    }

    /**
     * Returns a Promise which is already completed with the given exception.
     *
     * @param exception the exception
     * @param <U>       the result type
     * @return the new completed promise
     */
    static <U> Promise<U> exceptionally(Throwable exception) {
        return HelperPromise.exceptionally(exception);
    }

    /**
     * Returns a Promise which represents the given future.
     *
     * <p>The implementation will make an attempt to wrap the future without creating a new process
     * to await the result (by casting to {@link java.util.concurrent.CompletionStage} or
     * {@link com.google.common.util.concurrent.ListenableFuture}).</p>
     *
     * <p>Calls to {@link #cancel() cancel} the returned promise will not affected the wrapped
     * future.</p>
     *
     * @param future the future to wrap
     * @param <U>    the result type
     * @return the new promise
     */
    static <U> Promise<U> wrapFuture(Future<U> future) {
        return HelperPromise.wrapFuture(future);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier.
     *
     * @param context  the type of executor to use to supply the promise
     * @param supplier the value supplier
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplying(ThreadContext context, Supplier<U> supplier) {
        Promise<U> p = empty();
        return p.supply(context, supplier);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier.
     *
     * @param supplier the value supplier
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingSync(Supplier<U> supplier) {
        Promise<U> p = empty();
        return p.supplySync(supplier);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier.
     *
     * @param supplier the value supplier
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingAsync(Supplier<U> supplier) {
        Promise<U> p = empty();
        return p.supplyAsync(supplier);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param supplier   the value supplier
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingDelayed(ThreadContext context, Supplier<U> supplier, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyDelayed(context, supplier, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param context  the type of executor to use to supply the promise
     * @param supplier the value supplier
     * @param delay    the delay
     * @param unit     the unit of delay
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingDelayed(ThreadContext context, Supplier<U> supplier, long delay, TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyDelayed(context, supplier, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier   the value supplier
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingDelayedSync(Supplier<U> supplier, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyDelayedSync(supplier, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delay    the delay
     * @param unit     the unit of delay
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingDelayedSync(Supplier<U> supplier, long delay, TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyDelayedSync(supplier, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier   the value supplier
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingDelayedAsync(Supplier<U> supplier, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyDelayedAsync(supplier, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delay    the delay
     * @param unit     the unit of delay
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingDelayedAsync(Supplier<U> supplier, long delay, TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyDelayedAsync(supplier, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable.
     *
     * @param context  the type of executor to use to supply the promise
     * @param callable the value callable
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionally(ThreadContext context, Callable<U> callable) {
        Promise<U> p = empty();
        return p.supplyExceptionally(context, callable);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable.
     *
     * @param callable the value callable
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallySync(Callable<U> callable) {
        Promise<U> p = empty();
        return p.supplyExceptionallySync(callable);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable.
     *
     * @param callable the value callable
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallyAsync(Callable<U> callable) {
        Promise<U> p = empty();
        return p.supplyExceptionallyAsync(callable);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param callable   the value callable
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallyDelayed(ThreadContext context, Callable<U> callable, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayed(context, callable, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param context  the type of executor to use to supply the promise
     * @param callable the value callable
     * @param delay    the delay
     * @param unit     the unit of delay
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallyDelayed(ThreadContext context, Callable<U> callable, long delay, TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayed(context, callable, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable   the value callable
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallyDelayedSync(Callable<U> callable, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedSync(callable, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the value callable
     * @param delay    the delay
     * @param unit     the unit of delay
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallyDelayedSync(Callable<U> callable, long delay, TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedSync(callable, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable   the value callable
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallyDelayedAsync(Callable<U> callable, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedAsync(callable, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the value callable
     * @param delay    the delay
     * @param unit     the unit of delay
     * @param <U>      the result type
     * @return the promise
     */
    static <U> Promise<U> supplyingExceptionallyDelayedAsync(Callable<U> callable, long delay, TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedAsync(callable, delay, unit);
    }

    /**
     * Attempts to cancel execution of this task.
     *
     * @return {@code false} if the task could not be cancelled, typically
     * because it has already completed normally;
     * {@code true} otherwise
     */
    default boolean cancel() {
        return cancel(true);
    }

    /**
     * Returns the result value when complete, or throws an
     * (unchecked) exception if completed exceptionally.
     *
     * <p>To better conform with the use of common functional forms, if a
     * computation involved in the completion of this
     * Promise threw an exception, this method throws an
     * (unchecked) {@link CompletionException} with the underlying
     * exception as its cause.</p>
     *
     * @return the result value
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException   if this future completed
     *                               exceptionally or a completion computation threw an exception
     */
    V join();

    /**
     * Returns the result value (or throws any encountered exception)
     * if completed, else returns the given valueIfAbsent.
     *
     * @param valueIfAbsent the value to return if not completed
     * @return the result value, if completed, else the given valueIfAbsent
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException   if this future completed
     *                               exceptionally or a completion computation threw an exception
     */
    V getNow(V valueIfAbsent);

    /**
     * Supplies the Promise's result.
     *
     * @param value the object to pass to the promise
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supply(@Nullable V value);

    /**
     * Supplies an exceptional result to the Promise.
     *
     * @param exception the exception to supply
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyException(Throwable exception);

    /**
     * Schedules the supply of the Promise's result, via the given supplier.
     *
     * @param context  the type of executor to use to supply the promise
     * @param supplier the supplier
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    default Promise<V> supply(ThreadContext context, Supplier<V> supplier) {
        switch (context) {
            case SYNC:
                return supplySync(supplier);
            case ASYNC:
                return supplyAsync(supplier);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given supplier.
     *
     * @param supplier the supplier
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplySync(Supplier<V> supplier);

    /**
     * Schedules the supply of the Promise's result, via the given supplier.
     *
     * @param supplier the supplier
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyAsync(Supplier<V> supplier);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param supplier   the supplier
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    default Promise<V> supplyDelayed(ThreadContext context, Supplier<V> supplier, long delayTicks) {
        switch (context) {
            case SYNC:
                return supplyDelayedSync(supplier, delayTicks);
            case ASYNC:
                return supplyDelayedAsync(supplier, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param context  the type of executor to use to supply the promise
     * @param supplier the supplier
     * @param delay    the delay
     * @param unit     the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    default Promise<V> supplyDelayed(ThreadContext context, Supplier<V> supplier, long delay, TimeUnit unit) {
        switch (context) {
            case SYNC:
                return supplyDelayedSync(supplier, delay, unit);
            case ASYNC:
                return supplyDelayedAsync(supplier, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier   the supplier
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyDelayedSync(Supplier<V> supplier, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delay    the delay
     * @param unit     the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyDelayedSync(Supplier<V> supplier, long delay, TimeUnit unit);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier   the supplier
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyDelayedAsync(Supplier<V> supplier, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delay    the delay
     * @param unit     the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyDelayedAsync(Supplier<V> supplier, long delay, TimeUnit unit);

    /**
     * Schedules the supply of the Promise's result, via the given callable.
     *
     * @param context  the type of executor to use to supply the promise
     * @param callable the callable
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    default Promise<V> supplyExceptionally(ThreadContext context, Callable<V> callable) {
        switch (context) {
            case SYNC:
                return supplyExceptionallySync(callable);
            case ASYNC:
                return supplyExceptionallyAsync(callable);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given callable.
     *
     * @param callable the callable
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyExceptionallySync(Callable<V> callable);

    /**
     * Schedules the supply of the Promise's result, via the given callable.
     *
     * @param callable the callable
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyExceptionallyAsync(Callable<V> callable);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param callable   the callable
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    default Promise<V> supplyExceptionallyDelayed(ThreadContext context, Callable<V> callable, long delayTicks) {
        switch (context) {
            case SYNC:
                return supplyExceptionallyDelayedSync(callable, delayTicks);
            case ASYNC:
                return supplyExceptionallyDelayedAsync(callable, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param context  the type of executor to use to supply the promise
     * @param callable the callable
     * @param delay    the delay
     * @param unit     the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    default Promise<V> supplyExceptionallyDelayed(ThreadContext context, Callable<V> callable, long delay, TimeUnit unit) {
        switch (context) {
            case SYNC:
                return supplyExceptionallyDelayedSync(callable, delay, unit);
            case ASYNC:
                return supplyExceptionallyDelayedAsync(callable, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable   the callable
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyExceptionallyDelayedSync(Callable<V> callable, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the callable
     * @param delay    the delay
     * @param unit     the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyExceptionallyDelayedSync(Callable<V> callable, long delay, TimeUnit unit);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable   the callable
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyExceptionallyDelayedAsync(Callable<V> callable, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the callable
     * @param delay    the delay
     * @param unit     the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    Promise<V> supplyExceptionallyDelayedAsync(Callable<V> callable, long delay, TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn      the function to use to compute the value
     * @param <U>     the result type
     * @return the new promise
     */
    default <U> Promise<U> thenApply(ThreadContext context, Function<? super V, ? extends U> fn) {
        switch (context) {
            case SYNC:
                return thenApplySync(fn);
            case ASYNC:
                return thenApplyAsync(fn);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn  the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    <U> Promise<U> thenApplySync(Function<? super V, ? extends U> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn  the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    <U> Promise<U> thenApplyAsync(Function<? super V, ? extends U> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param fn         the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the new promise
     */
    default <U> Promise<U> thenApplyDelayed(ThreadContext context, Function<? super V, ? extends U> fn, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenApplyDelayedSync(fn, delayTicks);
            case ASYNC:
                return thenApplyDelayedAsync(fn, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn      the function to use to compute the value
     * @param delay   the delay
     * @param unit    the unit of delay
     * @param <U>     the result type
     * @return the new promise
     */
    default <U> Promise<U> thenApplyDelayed(ThreadContext context, Function<? super V, ? extends U> fn, long delay, TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenApplyDelayedSync(fn, delay, unit);
            case ASYNC:
                return thenApplyDelayedAsync(fn, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn         the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the new promise
     */
    <U> Promise<U> thenApplyDelayedSync(Function<? super V, ? extends U> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn    the function to use to compute the value
     * @param delay the delay
     * @param unit  the unit of delay
     * @param <U>   the result type
     * @return the new promise
     */
    <U> Promise<U> thenApplyDelayedSync(Function<? super V, ? extends U> fn, long delay, TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn         the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the new promise
     */
    <U> Promise<U> thenApplyDelayedAsync(Function<? super V, ? extends U> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn    the function to use to compute the value
     * @param delay the delay
     * @param unit  the unit of delay
     * @param <U>   the result type
     * @return the new promise
     */
    <U> Promise<U> thenApplyDelayedAsync(Function<? super V, ? extends U> fn, long delay, TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action.
     *
     * @param context the type of executor to use to supply the promise
     * @param action  the action to perform before completing the returned future
     * @return the new promise
     */
    default Promise<Void> thenAccept(ThreadContext context, Consumer<? super V> action) {
        switch (context) {
            case SYNC:
                return thenAcceptSync(action);
            case ASYNC:
                return thenAcceptAsync(action);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action.
     *
     * @param action the action to perform before completing the returned future
     * @return the new promise
     */
    default Promise<Void> thenAcceptSync(Consumer<? super V> action) {
        return thenApplySync(Delegates.consumerToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action.
     *
     * @param action the action to perform before completing the returned future
     * @return the new promise
     */
    default Promise<Void> thenAcceptAsync(Consumer<? super V> action) {
        return thenApplyAsync(Delegates.consumerToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param action     the action to perform before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    default Promise<Void> thenAcceptDelayed(ThreadContext context, Consumer<? super V> action, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenAcceptDelayedSync(action, delayTicks);
            case ASYNC:
                return thenAcceptDelayedAsync(action, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param action  the action to perform before completing the returned future
     * @param delay   the delay
     * @param unit    the unit of delay
     * @return the new promise
     */
    default Promise<Void> thenAcceptDelayed(ThreadContext context, Consumer<? super V> action, long delay, TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenAcceptDelayedSync(action, delay, unit);
            case ASYNC:
                return thenAcceptDelayedAsync(action, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action     the action to perform before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    default Promise<Void> thenAcceptDelayedSync(Consumer<? super V> action, long delayTicks) {
        return thenApplyDelayedSync(Delegates.consumerToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action the action to perform before completing the returned future
     * @param delay  the delay
     * @param unit   the unit of delay
     * @return the new promise
     */
    default Promise<Void> thenAcceptDelayedSync(Consumer<? super V> action, long delay, TimeUnit unit) {
        return thenApplyDelayedSync(Delegates.consumerToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action     the action to perform before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    default Promise<Void> thenAcceptDelayedAsync(Consumer<? super V> action, long delayTicks) {
        return thenApplyDelayedAsync(Delegates.consumerToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action the action to perform before completing the returned future
     * @param delay  the delay
     * @param unit   the unit of delay
     * @return the new promise
     */
    default Promise<Void> thenAcceptDelayedAsync(Consumer<? super V> action, long delay, TimeUnit unit) {
        return thenApplyDelayedAsync(Delegates.consumerToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task.
     *
     * @param context the type of executor to use to supply the promise
     * @param action  the action to run before completing the returned future
     * @return the new promise
     */
    default Promise<Void> thenRun(ThreadContext context, Runnable action) {
        switch (context) {
            case SYNC:
                return thenRunSync(action);
            case ASYNC:
                return thenRunAsync(action);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task.
     *
     * @param action the action to run before completing the returned future
     * @return the new promise
     */
    default Promise<Void> thenRunSync(Runnable action) {
        return thenApplySync(Delegates.runnableToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task.
     *
     * @param action the action to run before completing the returned future
     * @return the new promise
     */
    default Promise<Void> thenRunAsync(Runnable action) {
        return thenApplyAsync(Delegates.runnableToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param action     the action to run before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    default Promise<Void> thenRunDelayed(ThreadContext context, Runnable action, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenRunDelayedSync(action, delayTicks);
            case ASYNC:
                return thenRunDelayedAsync(action, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param action  the action to run before completing the returned future
     * @param delay   the delay
     * @param unit    the unit of delay
     * @return the new promise
     */
    default Promise<Void> thenRunDelayed(ThreadContext context, Runnable action, long delay, TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenRunDelayedSync(action, delay, unit);
            case ASYNC:
                return thenRunDelayedAsync(action, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action     the action to run before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    default Promise<Void> thenRunDelayedSync(Runnable action, long delayTicks) {
        return thenApplyDelayedSync(Delegates.runnableToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delay  the delay
     * @param unit   the unit of delay
     * @return the new promise
     */
    default Promise<Void> thenRunDelayedSync(Runnable action, long delay, TimeUnit unit) {
        return thenApplyDelayedSync(Delegates.runnableToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action     the action to run before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    default Promise<Void> thenRunDelayedAsync(Runnable action, long delayTicks) {
        return thenApplyDelayedAsync(Delegates.runnableToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delay  the delay
     * @param unit   the unit of delay
     * @return the new promise
     */
    default Promise<Void> thenRunDelayedAsync(Runnable action, long delay, TimeUnit unit) {
        return thenApplyDelayedAsync(Delegates.runnableToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn      the function to use to compute the value
     * @param <U>     the result type
     * @return the new promise
     */
    default <U> Promise<U> thenCompose(ThreadContext context, Function<? super V, ? extends Promise<U>> fn) {
        switch (context) {
            case SYNC:
                return thenComposeSync(fn);
            case ASYNC:
                return thenComposeAsync(fn);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn  the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    <U> Promise<U> thenComposeSync(Function<? super V, ? extends Promise<U>> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn  the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    <U> Promise<U> thenComposeAsync(Function<? super V, ? extends Promise<U>> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context    the type of executor to use to supply the promise
     * @param fn         the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the new promise
     */
    default <U> Promise<U> thenComposeDelayedSync(ThreadContext context, Function<? super V, ? extends Promise<U>> fn, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenComposeDelayedSync(fn, delayTicks);
            case ASYNC:
                return thenComposeDelayedAsync(fn, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn      the function to use to compute the value
     * @param delay   the delay
     * @param unit    the unit of delay
     * @param <U>     the result type
     * @return the new promise
     */
    default <U> Promise<U> thenComposeDelayedSync(ThreadContext context, Function<? super V, ? extends Promise<U>> fn, long delay, TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenComposeDelayedSync(fn, delay, unit);
            case ASYNC:
                return thenComposeDelayedAsync(fn, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn         the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the new promise
     */
    <U> Promise<U> thenComposeDelayedSync(Function<? super V, ? extends Promise<U>> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn    the function to use to compute the value
     * @param delay the delay
     * @param unit  the unit of delay
     * @param <U>   the result type
     * @return the new promise
     */
    <U> Promise<U> thenComposeDelayedSync(Function<? super V, ? extends Promise<U>> fn, long delay, TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn         the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U>        the result type
     * @return the new promise
     */
    <U> Promise<U> thenComposeDelayedAsync(Function<? super V, ? extends Promise<U>> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn    the function to use to compute the value
     * @param delay the delay
     * @param unit  the unit of delay
     * @param <U>   the result type
     * @return the new promise
     */
    <U> Promise<U> thenComposeDelayedAsync(Function<? super V, ? extends Promise<U>> fn, long delay, TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function. Otherwise, if this promise completes normally, then the
     * returned promise also completes normally with the same value.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn      the function to use to compute the value of the returned
     *                Promise, if this promise completed exceptionally
     * @return the new promise
     */
    default Promise<V> exceptionally(ThreadContext context, Function<Throwable, ? extends V> fn) {
        switch (context) {
            case SYNC:
                return exceptionallySync(fn);
            case ASYNC:
                return exceptionallyAsync(fn);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function. Otherwise, if this promise completes normally, then the
     * returned promise also completes normally with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @return the new promise
     */
    Promise<V> exceptionallySync(Function<Throwable, ? extends V> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function. Otherwise, if this promise completes normally, then the
     * returned promise also completes normally with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @return the new promise
     */
    Promise<V> exceptionallyAsync(Function<Throwable, ? extends V> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param context    the type of executor to use to supply the promise
     * @param fn         the function to use to compute the value of the returned
     *                   Promise, if this promise completed exceptionally
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    default Promise<V> exceptionallyDelayed(ThreadContext context, Function<Throwable, ? extends V> fn, long delayTicks) {
        switch (context) {
            case SYNC:
                return exceptionallyDelayedSync(fn, delayTicks);
            case ASYNC:
                return exceptionallyDelayedAsync(fn, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn      the function to use to compute the value of the returned
     *                Promise, if this promise completed exceptionally
     * @param delay   the delay
     * @param unit    the unit of delay
     * @return the new promise
     */
    default Promise<V> exceptionallyDelayed(ThreadContext context, Function<Throwable, ? extends V> fn, long delay, TimeUnit unit) {
        switch (context) {
            case SYNC:
                return exceptionallyDelayedSync(fn, delay, unit);
            case ASYNC:
                return exceptionallyDelayedAsync(fn, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn         the function to use to compute the value of the returned
     *                   Promise, if this promise completed exceptionally
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    Promise<V> exceptionallyDelayedSync(Function<Throwable, ? extends V> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn    the function to use to compute the value of the returned
     *              Promise, if this promise completed exceptionally
     * @param delay the delay
     * @param unit  the unit of delay
     * @return the new promise
     */
    Promise<V> exceptionallyDelayedSync(Function<Throwable, ? extends V> fn, long delay, TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn         the function to use to compute the value of the returned
     *                   Promise, if this promise completed exceptionally
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    Promise<V> exceptionallyDelayedAsync(Function<Throwable, ? extends V> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn    the function to use to compute the value of the returned
     *              Promise, if this promise completed exceptionally
     * @param delay the delay
     * @param unit  the unit of delay
     * @return the new promise
     */
    Promise<V> exceptionallyDelayedAsync(Function<Throwable, ? extends V> fn, long delay, TimeUnit unit);


    /**
     * Returns a {@link CompletableFuture} maintaining the same
     * completion properties as this Promise.
     * <p>
     * A Promise implementation that does not choose to interoperate
     * with CompletableFutures may throw {@code UnsupportedOperationException}.
     *
     * @return the CompletableFuture
     * @throws UnsupportedOperationException if this implementation
     *                                       does not interoperate with CompletableFuture
     */
    CompletableFuture<V> toCompletableFuture();

}
