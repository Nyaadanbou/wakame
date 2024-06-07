package cc.mewcraft.wakame.skill.state

import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.toCombo
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.RingBuffer
import me.lucko.helper.text3.mini
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

interface SkillStateManager {
    fun addTrigger(trigger: Trigger, skillCastFunction: (Skill) -> Boolean)
}

class PlayerSkillStateManager(
    val user: User<Player>
) : SkillStateManager, KoinComponent {
    private val triggers: RingBuffer<Trigger> = RingBuffer(3)

    override fun addTrigger(trigger: Trigger, skillCastFunction: (Skill) -> Boolean) {
        triggers.write(trigger)
        val bufferTriggers = triggers.readAll()
        val player = user.player
        displayProgress(bufferTriggers, player)
        if (!triggers.isFull())
            return

        val combo = bufferTriggers.toCombo()
        val skills = user.skillMap.getSkill(combo).takeUnlessEmpty()
        if (skills == null) {
            triggers.clear()
            displayFail(bufferTriggers, player)
            return
        }
        val result = skills.map { skill -> skillCastFunction.invoke(skill) }
        if (result.contains(false)) {
            triggers.clear()
            displayFail(bufferTriggers, player)
            return
        }

        displaySuccess(bufferTriggers, player)
        triggers.clear()
    }

    private fun displayProgress(triggers: List<Trigger>, player: Player) {
        // TODO: Configurable
        player.sendActionBar(triggers.joinToString("<gray>-") { "<green>${it.id}" }.mini)
    }

    private fun displaySuccess(triggers: List<Trigger>, player: Player) {
        // TODO: Configurable

    }

    private fun displayFail(triggers: List<Trigger>, player: Player) {
        // TODO: Configurable
        player.sendActionBar(triggers.joinToString("<gray>-") { "<red>${it.id}" }.mini)
    }
}