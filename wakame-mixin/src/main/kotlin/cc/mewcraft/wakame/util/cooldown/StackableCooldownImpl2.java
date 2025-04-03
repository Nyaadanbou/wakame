package cc.mewcraft.wakame.util.cooldown;

import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

@NullMarked
public class StackableCooldownImpl2 implements StackableCooldown {

    private final Cooldown base;
    private final Supplier<Long> stacks;

    StackableCooldownImpl2(Cooldown base, Supplier<Long> stacks) {
        this.base = base.copy();
        this.stacks = stacks;
    }

    @Override
    public Cooldown getBase() {
        return base;
    }

    @Override
    public long getStacks() {
        return stacks.get();
    }

}
