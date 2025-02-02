package cc.mewcraft.wakame.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class KoishLoadDataEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public KoishLoadDataEvent() {
        super(false);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}