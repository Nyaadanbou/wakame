package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DefenseMetadata
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class WakameEntityDamageEvent(
    val damagee: Entity,
    val damager: Entity?,
    var damageMetadata: DamageMetadata,
    var defenseMetadata: DefenseMetadata
) : Event(), Cancellable {
    private var cancel: Boolean = false

    val finalDamage: Double
        get() = defenseMetadata.calculateFinalDamage(damageMetadata);

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}