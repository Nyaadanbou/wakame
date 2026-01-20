package cc.mewcraft.extracontexts.paper;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class OpCalculator implements ContextCalculator<Player> {
    private static final String KEY = "is-op";

    @Override
    public void calculate(Player target, ContextConsumer consumer) {
        consumer.accept(KEY, String.valueOf(target.isOp()));
    }

    @Override
    public @NotNull ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
            .add(KEY, "true")
            .add(KEY, "false")
            .build();
    }
}