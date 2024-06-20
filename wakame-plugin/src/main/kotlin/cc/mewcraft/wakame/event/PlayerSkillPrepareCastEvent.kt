package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.Target
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class PlayerSkillPrepareCastEvent(
    skill: Skill,
    val caster: Player,
    val target: Target?,
    val item: ItemStack?
) : SkillPrepareCastEvent(skill) {
    
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