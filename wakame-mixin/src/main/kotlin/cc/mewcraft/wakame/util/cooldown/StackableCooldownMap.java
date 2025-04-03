package cc.mewcraft.wakame.util.cooldown;

import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A self-populating map of stackable cooldown instances
 *
 * @param <T> the type
 */
@NullMarked
public interface StackableCooldownMap<T> {

    static <T> StackableCooldownMap<T> create(Cooldown base, long stacks) {
        Objects.requireNonNull(base, "base");
        return new StackableCooldownMapImpl<>(base, stacks);
    }

    static <T> StackableCooldownMap<T> create(Cooldown base, Supplier<Long> stacks) {
        Objects.requireNonNull(base, "base");
        return new StackableCooldownMapImpl<>(base, stacks);
    }

    static <T> StackableCooldownMap<T> create(Cooldown base, Function<T, Long> stacks) {
        Objects.requireNonNull(base, "base");
        return new StackableCooldownMapImpl<>(base, stacks);
    }

    /**
     * Gets the base stackable cooldown
     *
     * @return the base stackable cooldown
     */
    Cooldown getBase();

    /**
     * Gets the internal stackable cooldown instance associated with the given key.
     *
     * <p>The inline Cooldown methods in this class should be used to access the functionality of the stackable
     * cooldown as opposed to calling the methods directly via the instance returned by this method.
     *
     * @param key the key
     * @return a stackable cooldown instance
     */
    StackableCooldown get(T key);

    void put(T key, StackableCooldown cooldown);

    /**
     * Gets the stackable cooldowns contained within this collection.
     *
     * @return the backing map
     */
    Map<T, StackableCooldown> getAll();

    /* methods from StackableCooldown */

    default boolean test(T key) {
        return get(key).test();
    }

    default boolean testSilently(T key) {
        return get(key).testSilently();
    }

    default long elapsed(T key) {
        return get(key).elapsed();
    }

    default void consumeOne(T key) {
        get(key).consumeOne();
    }

    default void consumeAll(T key) {
        get(key).consumeAll();
    }

    default long remainingMillis(T key) {
        return get(key).remainingMillis();
    }

    default long remainingMillisAll(T key) {
        return get(key).remainingMillisAll();
    }

    default long remainingTime(T key, TimeUnit unit) {
        return get(key).remainingTime(unit);
    }

    default long remainingTimeAll(T key, TimeUnit unit) {
        return get(key).remainingTimeAll(unit);
    }

    default OptionalLong getLastTested(T key) {
        return get(key).getLastTested();
    }

    default void setLastTested(T key, long time) {
        get(key).setLastTested(time);
    }

}
