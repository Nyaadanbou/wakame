package cc.mewcraft.wakame.util.cooldown;

import org.jspecify.annotations.NullMarked;

import java.util.function.Function;

@NullMarked
public class StackableCooldownImpl3<T> implements StackableCooldown {

    private final Cooldown base;
    private final T key;
    private final Function<T, Long> stacks;

    StackableCooldownImpl3(Cooldown base, T key, Function<T, Long> stacks) {
        this.base = base.copy();
        this.key = key;
        this.stacks = stacks;
    }

    @Override
    public Cooldown getBase() {
        return base;
    }

    @Override
    public long getStacks() {
        return stacks.apply(key);
    }

}