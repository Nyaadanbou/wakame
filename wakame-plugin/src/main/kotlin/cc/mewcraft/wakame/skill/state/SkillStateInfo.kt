package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillCastManager
import cc.mewcraft.wakame.skill.SkillTick
import cc.mewcraft.wakame.skill.TickResult
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.util.RingBuffer
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 代表了一个玩家技能状态的信息.
 */
sealed interface SkillStateInfo {
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
            return IdleStateInfo(state)
        }
    }
}

/**
 * 表示玩家技能状态的空闲状态(无施法), 即玩家可以使用技能.
 */
class IdleStateInfo(
    private val state: PlayerSkillState,
) : SkillStateInfo, KoinComponent {
    companion object {
        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val skillStateShower: SkillStateShower<Player> by inject()
    private val skillCastManager: SkillCastManager by inject()

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(3)

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        val user = state.user
        // TODO: Make sure the player is not spamming the skill

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

        val skillTick = skillCastManager.tryCast(skills.first(), context).skillTick

        state.setInfo(CastPointStateInfo(state, skillTick))
        return SkillStateResult.CANCEL_EVENT
    }

    override fun tick() = Unit

    override fun interrupt() {
        currentSequence.clear()
        skillStateShower.displayFailure(currentSequence.readAll(), state.user)
    }
}

/**
 * 表示玩家技能状态的前摇状态, 即玩家正在试图使用技能.
 */
class CastPointStateInfo(
    private val state: PlayerSkillState,
    private val skillTick: SkillTick,
) : SkillStateInfo {

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        interrupt()
        return SkillStateResult.SUCCESS
    }

    override fun tick() {
        val result = skillTick.tick()
        when (result) {
            TickResult.CONTINUE_TICK -> return
            TickResult.ALL_DONE -> state.setInfo(CastStateInfo(state, skillTick))
            TickResult.INTERRUPT -> interrupt()
        }
    }

    override fun interrupt() {
        state.setInfo(IdleStateInfo(state))
    }
}

/**
 * 表示玩家技能状态的释放状态, 即玩家正在释放技能.
 */
class CastStateInfo(
    private val state: PlayerSkillState,
    private val skillTick: SkillTick,
) : SkillStateInfo {

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        return SkillStateResult.CANCEL_EVENT
    }

    override fun tick() {
        val result = skillTick.tick()
        when (result) {
            TickResult.CONTINUE_TICK -> return
            TickResult.ALL_DONE -> state.setInfo(BackswingStateInfo(state, skillTick))
            TickResult.INTERRUPT -> interrupt()
        }
    }

    override fun interrupt() {
        state.setInfo(IdleStateInfo(state))
    }
}

/**
 * 表示玩家技能状态的后摇状态, 即玩家释放技能后的状态.
 */
class BackswingStateInfo(
    private val state: PlayerSkillState,
    private val skillTick: SkillTick,
) : SkillStateInfo {

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        interrupt()
        return SkillStateResult.SUCCESS
    }

    override fun tick() {
        val result = skillTick.tick()
        when (result) {
            TickResult.CONTINUE_TICK -> return
            TickResult.ALL_DONE -> state.setInfo(IdleStateInfo(state))
            TickResult.INTERRUPT -> interrupt()
        }
    }

    override fun interrupt() {
        state.setInfo(IdleStateInfo(state))
    }
}
