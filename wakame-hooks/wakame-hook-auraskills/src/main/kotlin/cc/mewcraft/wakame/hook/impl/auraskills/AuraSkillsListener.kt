package cc.mewcraft.wakame.hook.impl.auraskills

import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelUpEvent
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AuraSkillsListener : Listener {

    @EventHandler
    fun on(event: SkillLevelUpEvent) {
        PlayerLevelUpEvent(event.player, event.skill.name(), event.level).callEvent()
    }
}