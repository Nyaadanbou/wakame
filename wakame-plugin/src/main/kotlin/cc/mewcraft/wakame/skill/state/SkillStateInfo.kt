package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillCastManager
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.util.RingBuffer
import me.lucko.helper.cooldown.Cooldown
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 代表了一个玩家技能状态的信息.
 */
interface SkillStateInfo {
    /**
     * 添加一个 [SingleTrigger],
     */
    fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult

    /**
     * 进行一次状态的刷新.
     */
    fun tick()

    /**
     * 中断当前的状态.
     */
    fun interrupt()

    companion object {
        /**
         * 创建一个空的 [SkillStateInfo] 实例.
         */
        fun idle(state: PlayerSkillState): SkillStateInfo {
            return Idle(state, 2)
        }
    }
}

/**
 * 表示玩家技能状态的空闲状态(无施法), 即玩家可以使用技能.
 */
private class Idle(
    private val state: PlayerSkillState,
    cooldownTicks: Long
) : SkillStateInfo, KoinComponent {
    companion object {
        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val skillStateShower: SkillStateShower<Player> by inject()
    private val cooldown: Cooldown = Cooldown.ofTicks(cooldownTicks)
    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(3)

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        val user = state.user
        // To make sure the player is not spamming the skill
        if (!cooldown.test())
            return SkillStateResult.SILENT_FAILURE

        val skills = mutableListOf<Skill>()
        if (trigger in SEQUENCE_GENERATION_TRIGGERS) {
            // If the trigger is a sequence generation trigger, we should add it to the sequence
            currentSequence.write(trigger)
            val completeSequence = currentSequence.readAll()
            skillStateShower.displayProgress(completeSequence, user)

            if (currentSequence.isFull()) {
                val sequence = SequenceTrigger.of(completeSequence)
                val skillsOnSequence = user.skillMap.getSkill(sequence)
                skills.addAll(skillsOnSequence)
                currentSequence.clear()
            }
        }

        val skillsOnSingle = user.skillMap.getSkill(trigger)
        skills.addAll(skillsOnSingle)

        if (skills.isEmpty())
            return SkillStateResult.SILENT_FAILURE

        state.setInfo(BeforeCasting(state, skills, context, 1))
        return SkillStateResult.CANCEL_EVENT
    }

    override fun tick() = Unit

    override fun interrupt() {
        cooldown.reset()
        currentSequence.clear()
        skillStateShower.displayFailure(currentSequence.readAll(), state.user)
    }
}

/**
 * 表示玩家技能状态的前摇状态, 即玩家正在试图使用技能.
 */
private class BeforeCasting(
    private val state: PlayerSkillState,
    /**
     * 将要释放的技能.
     */
    private val skills: Collection<Skill>,
    /**
     * 技能的上下文
     */
    private val context: SkillCastContext,
    /**
     * 此状态的持续时间.
     */
    waitingTicks: Long
) : SkillStateInfo {
    private var counter: Long = waitingTicks

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        interrupt()
        return SkillStateResult.SUCCESS
    }

    override fun tick() {
        counter--
        if (counter <= 0) {
            state.setInfo(Casting(state, skills, context, 1))
        }
    }

    override fun interrupt() {
        state.setInfo(Idle(state, 2))
    }
}

/**
 * 表示玩家技能状态的释放状态, 即玩家正在释放技能.
 */
private class Casting(
    private val state: PlayerSkillState,
    private val skills: Collection<Skill>,
    /**
     * 技能的上下文
     */
    private val context: SkillCastContext,
    /**
     * 此状态的持续时间.
     */
    castingTicks: Long
) : SkillStateInfo, KoinComponent {
    private val skillCastManager: SkillCastManager by inject()
    private var counter: Long = castingTicks

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        return SkillStateResult.CANCEL_EVENT
    }

    override fun tick() {
        counter--
        if (counter <= 0) {
            skills.forEach { skillCastManager.tryCast(it, context) }
            state.setInfo(AfterCasting(state, 1))
        }
    }

    override fun interrupt() {
        state.setInfo(Idle(state, 2))
    }
}

/**
 * 表示玩家技能状态的后摇状态, 即玩家释放技能后的状态.
 */
private class AfterCasting(
    private val state: PlayerSkillState,
    /**
     * 此状态的持续时间.
     */
    waitingTicks: Long
) : SkillStateInfo {
    private var counter: Long = waitingTicks

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        interrupt()
        return SkillStateResult.SUCCESS
    }

    override fun tick() {
        counter--
        if (counter <= 0) {
            state.setInfo(Idle(state, 2))
        }
    }

    override fun interrupt() {
        state.setInfo(Idle(state, 2))
    }
}
