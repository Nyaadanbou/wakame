package cc.mewcraft.wakame.event;

import cc.mewcraft.wakame.world.attribute.damage.DamageMetaData;
import cc.mewcraft.wakame.world.attribute.damage.DefenseMetaData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WakameEntityDamageEvent extends Event implements Cancellable {

    private static final @NotNull HandlerList HANDLERS = new HandlerList();

    private Boolean isCancelled = false;

    private DamageMetaData damageMetaData;
    private DefenseMetaData defenseMetaData;

    public WakameEntityDamageEvent(DamageMetaData damageMetaData, DefenseMetaData defenseMetaData) {
        this.damageMetaData = damageMetaData;
        this.defenseMetaData = defenseMetaData;
    }


    public DamageMetaData getDamageMetaData() {
        return damageMetaData;
    }

    public DefenseMetaData getDefenseMetaData() {
        return defenseMetaData;
    }

    public void setDamageMetaData(DamageMetaData damageMetaData) {
        this.damageMetaData = damageMetaData;
    }

    public void setDefenseMetaData(DefenseMetaData defenseMetaData) {
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
