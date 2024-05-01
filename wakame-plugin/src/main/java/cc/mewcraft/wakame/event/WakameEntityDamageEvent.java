package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.world.attribute.damage.DamageMetaData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public class WakameEntityDamageEvent extends Event implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private final EntityDamageEvent event;
    private final DamageMetaData damageMetaData;

    public WakameEntityDamageEvent(EntityDamageEvent event, DamageMetaData damageMetaData) {
        this.event = event;
        this.damageMetaData = damageMetaData;
    }

    public EntityDamageEvent getBukkitEvent() {
        return event;
    }

    public DamageMetaData getDamageMetaData() {
        return damageMetaData;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        event.setCancelled(cancelled);
    }
}
