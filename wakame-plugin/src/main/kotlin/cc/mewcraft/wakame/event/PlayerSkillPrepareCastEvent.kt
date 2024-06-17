package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

// FIXME 这个事件原本是要暴露在API里的, 因此把 SkillCastContext 写在这里不是很好
class PlayerSkillPrepareCastEvent(
    skill: Skill,
    context: SkillCastContext
) : SkillPrepareCastEvent(skill, context) {

    val caster: Player = context.get(SkillCastContextKey.CASTER_PLAYER).bukkitPlayer
    val target: Target? = context.optional(SkillCastContextKey.TARGET)
    val item: ItemStack? = context.optional(SkillCastContextKey.ITEM_STACK)

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}