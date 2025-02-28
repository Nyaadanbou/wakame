package cc.mewcraft.wakame.util.cooldown;

import cc.mewcraft.wakame.util.time.Time;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@NullMarked
public interface StackableCooldown {

    static StackableCooldown of(Cooldown base, long stacks) {
        return new StackableCooldownImpl1(base, stacks);
    }

    static StackableCooldown of(Cooldown base, Supplier<Long> stacks) {
        return new StackableCooldownImpl2(base, stacks);
    }

    static <T> StackableCooldown of(Cooldown base, T key, Function<T, Long> stacks) {
        return new StackableCooldownImpl3<>(base, key, stacks);
    }

    /**
     * Gets the time until a single stack will become usable.
     *
     * @param unit the unit to return in
     * @return the time until a stack will be usable
     */
    default long remainingTime(TimeUnit unit) {
        return unit.convert(remainingMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * @see #remainingTime(TimeUnit)
     */
    default long remainingMillis() {
        return remainingMillisAll() % getBaseTimeout();
    }

    /**
     * Gets the time until all stacks will become usable.
     *
     * <p>If all stacks are usable, this method returns <code>0</code>.</p>
     *
     * @param unit the unit to return in
     * @return the time until all stacks will be usable
     */
    default long remainingTimeAll(TimeUnit unit) {
        return unit.convert(remainingMillisAll(), TimeUnit.MILLISECONDS);
    }

    /**
     * @see #remainingMillisAll()
     */
    default long remainingMillisAll() {
        return getStacks() * getBaseTimeout() - elapsed();
    }

    /**
     * Returns true if at least a single stack is usable, and then consumes a stack.
     *
     * <p>If there is no stack usable, the timer is <strong>not</strong>
     * set.</p>
     *
     * @return true if at least a single stack is usable
     */
    default boolean test() {
        if (testSilently()) {
            consumeOne();
            return true;
        }
        return false;
    }

    /**
     * Returns true if at least a single stack is usable
     *
     * @return true if at least a single stack is usable
     */
    default boolean testSilently() {
        return getAvailable() > 0;
    }

    /**
     * Consumes one stack, only if there is one.
     */
    default void consumeOne() {
        long stack = getAvailable();
        if (stack > 0) {
            long remainder = elapsed() % getBaseTimeout();
            long diff = (stack - 1) * getBaseTimeout() + remainder;
            setLastTested(Time.nowMillis() - diff);
        }
    }

    /**
     * Consumes all stacks, resetting the lastTested to now.
     */
    default void consumeAll() {
        setLastTested(Time.nowMillis());
    }

    default OptionalLong getLastTested() {
        return getBase().getLastTested();
    }

    /**
     * Sets the time in milliseconds when this base cooldown was last tested.
     *
     * <p>Note: this should only be used internally. Use {@link #test()} otherwise.</p>
     *
     * @param time the time
     */
    default void setLastTested(long time) {
        getBase().setLastTested(time);
    }

    /**
     * Returns the elapsed time in milliseconds since the cooldown was last reset, or the <b>total</b> elapsed time for
     * all stacks to be usable.
     *
     * @return the elapsed time
     */
    default long elapsed() {
        // if [elapsed] = [max stacks] * [base timeout], then it means none of the stacks is used yet
        return Math.min(getBase().elapsed(), getStacks() * getBaseTimeout());
    }

    default long elapsedOne() {
        return elapsed() % getBaseTimeout();
    }

    default long getBaseTimeout() {
        return getBase().getTimeout();
    }

    default long getBaseTimeout(TimeUnit unit) {
        return unit.convert(getBaseTimeout(), TimeUnit.MILLISECONDS);
    }

    /**
     * @return the amount of currently available stacks that can be consumed
     */
    default long getAvailable() {
        return elapsed() / getBaseTimeout();
    }

    /**
     * @return the base {@link Cooldown} instance
     */
    @ApiStatus.Internal
    Cooldown getBase();

    /**
     * @return the maximum number of stacks
     */
    long getStacks();

}
