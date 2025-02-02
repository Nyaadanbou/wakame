package cc.mewcraft.wakame.util.cooldown;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

@NullMarked
public class StackableCooldownMapImpl<T> implements StackableCooldownMap<T> {

    private final Cooldown base;
    private final LoadingCache<T, StackableCooldown> cache;

    StackableCooldownMapImpl(Cooldown base, long stacks) {
        this.base = base;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(base.getTimeout() * 1000L, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public StackableCooldown load(T key) {
                        return StackableCooldown.of(base, stacks);
                    }
                });
    }

    StackableCooldownMapImpl(Cooldown base, Supplier<Long> stacks) {
        this.base = base;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(base.getTimeout() * 1000L, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public StackableCooldown load(T key) {
                        return StackableCooldown.of(base, stacks);
                    }
                });
    }

    StackableCooldownMapImpl(Cooldown base, Function<T, Long> stacks) {
        this.base = base;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(base.getTimeout() * 1000L, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public StackableCooldown load(T key) {
                        return StackableCooldown.of(base, key, stacks);
                    }
                });
    }

    @Override
    public Cooldown getBase() {
        return base;
    }

    @Override
    public StackableCooldown get(T key) {
        return cache.getUnchecked(key);
    }

    @Override
    public void put(T key, StackableCooldown cooldown) {
        Objects.requireNonNull(key, "key");
        Preconditions.checkArgument(cooldown.getBaseTimeout() == this.base.getTimeout(), "different timeout");
        this.cache.put(key, cooldown);
    }

    @Override
    public Map<T, StackableCooldown> getAll() {
        return cache.asMap();
    }

}
