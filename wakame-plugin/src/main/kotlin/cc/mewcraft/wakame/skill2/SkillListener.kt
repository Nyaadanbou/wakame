package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.event.PlayerSkillStateChangeEvent
import cc.mewcraft.wakame.skill2.state.PlayerSkillState
import cc.mewcraft.wakame.user.toUser
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal class SkillListener : Listener {
    @EventHandler
    private fun onStateChange(event: PlayerSkillStateChangeEvent) {
        val user = event.player.toUser()
        val skillState = user.skillState as? PlayerSkillState ?: return

        skillState.onStateChange(event)
    }
}