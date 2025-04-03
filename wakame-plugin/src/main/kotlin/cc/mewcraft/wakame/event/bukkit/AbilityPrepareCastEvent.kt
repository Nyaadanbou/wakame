package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

abstract class AbilityPrepareCastEvent(
    val ability: AbilityMeta,
) : Event(), Cancellable {

    private var cancel: Boolean = false

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }
}