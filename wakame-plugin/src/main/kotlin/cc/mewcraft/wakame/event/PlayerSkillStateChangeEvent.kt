package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.skill2.Skill
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerSkillStateChangeEvent(
    player: Player,
    val skill: Skill,
    val oldPhase: StatePhase,
    val newPhase: StatePhase
) : PlayerEvent(player) {

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