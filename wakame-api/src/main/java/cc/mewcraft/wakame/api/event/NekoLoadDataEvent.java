package cc.mewcraft.wakame.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NekoLoadDataEvent extends Event {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    public NekoLoadDataEvent() {
        super(false);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

}