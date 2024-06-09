package cc.mewcraft.wakame.skill.state

import cc.mewcraft.commons.collections.takeUnlessEmpty
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.RingBuffer
import cc.mewcraft.wakame.util.toCombo
import me.lucko.helper.cooldown.Cooldown
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SkillStateManager {
    fun addTrigger(trigger: Trigger, skillCastFunction: (Skill) -> Boolean)
    fun clear()
}

class PlayerSkillStateManager(
    val user: User<Player>
) : SkillStateManager, KoinComponent {
    private val skillStateShower: SkillStateShower<Player> by inject()

    private val cooldown: Cooldown = Cooldown.ofTicks(2)
    private val triggers: RingBuffer<Trigger> = RingBuffer(3)

    override fun addTrigger(trigger: Trigger, skillCastFunction: (Skill) -> Boolean) {
        // To make sure the player is not spamming the skill
        if (!cooldown.test())
            return

        triggers.write(trigger)
        val bufferTriggers = triggers.readAll()
        skillStateShower.displayProgress(bufferTriggers, user)
        if (!triggers.isFull())
            return

        val combo = bufferTriggers.toCombo()
        val skills = user.skillMap.getSkill(combo).takeUnlessEmpty()
        if (skills == null) {
            clearAndRun { skillStateShower.displayFailure(bufferTriggers, user) }
            return
        }
        val result = skills.map { skill -> skillCastFunction.invoke(skill) }
        if (result.contains(false)) {
            clearAndRun { skillStateShower.displayFailure(bufferTriggers, user) }
            return
        }

        clearAndRun { skillStateShower.displaySuccess(bufferTriggers, user) }
    }

    override fun clear() {
        triggers.clear()
        cooldown.reset()
    }

    private fun clearAndRun(block: () -> Unit) {
        clear()
        block()
    }
}