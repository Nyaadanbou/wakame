package cc.mewcraft.wakame.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NekoReloadEvent extends Event {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public NekoReloadEvent() {
        super(true);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}
