package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.context.SkillCastContext
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

abstract class SkillPrepareCastEvent(
    val skill: Skill,
    val context: SkillCastContext,
) : Event(), Cancellable {

    private var cancel: Boolean = false

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }
}