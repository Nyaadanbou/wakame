package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.pack.generate.GenerationArgs;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a resource pack is being generated.
 * <p>
 * You can use this event to change {@link GenerationArgs} to modify the resource pack.
 * <p>
 * If you want to cancel the generation, you can use {@link #setCancelled(boolean)}.
 * <p>
 * If you want to
 */
public class ResourcePackGeneratingEvent extends Event implements Cancellable {
    private final GenerationArgs args;
    private boolean cancelled;

    public ResourcePackGeneratingEvent(GenerationArgs args) {
        this.args = args;
    }

    public GenerationArgs getArgs() {
        return args;
    }

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
