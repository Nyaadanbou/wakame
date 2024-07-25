package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.damage.DamageMetadata;
import cc.mewcraft.wakame.damage.DefenseMetadata;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WakameEntityDamageEvent extends Event implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private Boolean isCancelled = false;

    private DamageMetadata damageMetaData;
    private DefenseMetadata defenseMetaData;

    public WakameEntityDamageEvent(DamageMetadata damageMetaData, DefenseMetadata defenseMetaData) {
        this.damageMetaData = damageMetaData;
        this.defenseMetaData = defenseMetaData;
    }


    public DamageMetadata getDamageMetaData() {
        return damageMetaData;
    }

    public DefenseMetadata getDefenseMetaData() {
        return defenseMetaData;
    }

    public void setDamageMetaData(DamageMetadata damageMetaData) {
        this.damageMetaData = damageMetaData;
    }

    public void setDefenseMetaData(DefenseMetadata defenseMetaData) {
        this.defenseMetaData = defenseMetaData;
    }

    public double getFinalDamage() {
        return defenseMetaData.calculateFinalDamage(damageMetaData);
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
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
