package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.pack.generate.GenerationArgs;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a resource pack is being generated.
 * <p>
 * You can use this event to change {@link GenerationArgs} to modify the resource pack.
 * <p>
 * To cancel the generation, use {@link #setCancelled(boolean)}.
 */
public class ResourcePackGenerateEvent extends Event implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();
    private final GenerationArgs args;
    private boolean cancelled;

    public ResourcePackGenerateEvent(GenerationArgs args) {
        super(true);
        this.args = args;
    }

    public GenerationArgs getArgs() {
        return args;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}
