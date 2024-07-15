package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.skill.state.SkillStateInfo
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerSkillStateChangeEvent(
    player: Player,
    val oldState: SkillStateInfo,
    val newState: SkillStateInfo
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