package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.skill2.Skill
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

abstract class SkillPrepareCastEvent(
    val skill: Skill,
) : Event(), Cancellable {

    private var cancel: Boolean = false

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }
}