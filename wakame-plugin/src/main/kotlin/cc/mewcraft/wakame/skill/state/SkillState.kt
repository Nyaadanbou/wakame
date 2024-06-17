package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.RingBuffer
import me.lucko.helper.cooldown.Cooldown
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface SkillState {
    fun addTrigger(trigger: SingleTrigger, skillCastFunction: (Skill) -> Boolean)
    fun clear()
}

class PlayerSkillState(
    val user: User<Player>
) : SkillState, KoinComponent {
    private val skillStateShower: SkillStateShower<Player> by inject()

    private val cooldown: Cooldown = Cooldown.ofTicks(2)
    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(3)

    override fun addTrigger(trigger: SingleTrigger, skillCastFunction: (Skill) -> Boolean) {
        // To make sure the player is not spamming the skill
        if (!cooldown.test())
            return

        currentSequence.write(trigger)
        val completeSequence = currentSequence.readAll()
        skillStateShower.displayProgress(completeSequence, user)

        if (!currentSequence.isFull())
            return

        val sequence = SequenceTrigger.of(completeSequence)
        val skills = user.skillMap.getSkill(sequence)
        if (skills.isEmpty()) {
            clear()
            skillStateShower.displayFailure(completeSequence, user)
            return
        }
        
        val result = skills.map { skill -> skillCastFunction.invoke(skill) }
        if (result.contains(false)) {
            clear()
            skillStateShower.displayFailure(completeSequence, user)
            return
        }

        clear()
        skillStateShower.displaySuccess(completeSequence, user)
    }

    override fun clear() {
        currentSequence.clear()
        cooldown.reset()
    }
}