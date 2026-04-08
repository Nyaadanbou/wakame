package cc.mewcraft.wakame.util.cooldown;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class StackableCooldownImpl1 implements StackableCooldown {

    private final Cooldown base;
    private final long stacks;

    StackableCooldownImpl1(Cooldown base, long stacks) {
        this.base = base.copy();
        this.stacks = stacks;
    }

    @Override
    public Cooldown getBase() {
        return base;
    }

    @Override
    public long getStacks() {
        return stacks;
    }

}
