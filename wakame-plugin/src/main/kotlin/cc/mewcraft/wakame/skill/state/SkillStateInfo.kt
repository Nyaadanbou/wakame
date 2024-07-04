package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.tick.PlayerSkillTick
import cc.mewcraft.wakame.skill.tick.TickResult
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
     * 当前状态的类型.
     */
    val type: Type

    /**
     * 正在执行的 [PlayerSkillTick], 没有则返回 `null`.
     *
     * 同时也可以用来判断玩家是否正在施法状态.
     */
    val skillTick: PlayerSkillTick?
        get() = null

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

    enum class Type {
        /**
         * 很特殊的状态, 表示玩家可以使用技能.
         *
         * 无法拿到 [SkillTick] 实例.
         */
        IDLE,
        CAST_POINT,
        CAST,
        BACKSWING,
        ;
    }
}

sealed class AbstractSkillStateInfo(
    override val type: SkillStateInfo.Type
) : SkillStateInfo {
    protected inner class TriggerConditionManager(private val skillTick: PlayerSkillTick) {

        fun isForbidden(trigger: SingleTrigger): Boolean {
            return skillTick.isForbidden(type, trigger)
        }

        fun interrupt(trigger: SingleTrigger) {
            if (skillTick.isInterrupted(type, trigger)) {
                interrupt()
            }
        }
    }
}

/**
 * 表示玩家技能状态的空闲状态(无施法), 即玩家可以使用技能.
 */
class IdleStateInfo(
    private val state: PlayerSkillState,
) : AbstractSkillStateInfo(SkillStateInfo.Type.IDLE), KoinComponent {
    companion object {
        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val skillStateShower: SkillStateShower<Player> by inject()
    private val skillCastManager: SkillCastManager by inject()

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(3)

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        val user = state.user

        val castableSkills = mutableListOf<Skill>()
        val skillMap = user.skillMap
        if (skillMap.hasTrigger<SequenceTrigger>() && trigger in SEQUENCE_GENERATION_TRIGGERS) {
            // If the trigger is a sequence generation trigger, we should add it to the sequence
            currentSequence.write(trigger)
            val completeSequence = currentSequence.readAll()
            if (skillMap.getTriggers().filterIsInstance<SequenceTrigger>().any { it.isStartWith(completeSequence) }) {
                skillStateShower.displayProgress(completeSequence, user)

                if (currentSequence.isFull()) {
                    val sequence = SequenceTrigger.of(completeSequence)
                    val skillsOnSequence = skillMap.getSkill(sequence)
                    castableSkills.addAll(skillsOnSequence)
                    currentSequence.clear()
                }
            }
        }

        // Single trigger skills
        val skillsOnSingle = skillMap.getSkill(trigger)
        castableSkills.addAll(skillsOnSingle)

        if (castableSkills.isEmpty())
            return SkillStateResult.SILENT_FAILURE

        val skill = castableSkills.first()
        val skillTick = skillCastManager.tryCast(skill, context).skillTick as? PlayerSkillTick
            ?: return SkillStateResult.SILENT_FAILURE

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
    override val skillTick: PlayerSkillTick,
) : AbstractSkillStateInfo(SkillStateInfo.Type.CAST_POINT) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager(skillTick)

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        triggerConditionManager.interrupt(trigger)
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
    override val skillTick: PlayerSkillTick,
) : AbstractSkillStateInfo(SkillStateInfo.Type.CAST) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager(skillTick)

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        triggerConditionManager.interrupt(trigger)
        return SkillStateResult.SUCCESS
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
    override val skillTick: PlayerSkillTick,
) : AbstractSkillStateInfo(SkillStateInfo.Type.BACKSWING) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager(skillTick)

    override fun addTrigger(trigger: SingleTrigger, context: SkillCastContext): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        triggerConditionManager.interrupt(trigger)
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
