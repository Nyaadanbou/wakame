package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.ability.Ability
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

abstract class AbilityPrepareCastEvent(
    val ability: Ability,
) : Event(), Cancellable {

    private var cancel: Boolean = false

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }
}