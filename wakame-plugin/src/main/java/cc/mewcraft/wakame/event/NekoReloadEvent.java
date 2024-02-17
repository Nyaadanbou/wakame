package cc.mewcraft.wakame.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NekoReloadEvent extends Event {

    public NekoReloadEvent() {super(false);}

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
