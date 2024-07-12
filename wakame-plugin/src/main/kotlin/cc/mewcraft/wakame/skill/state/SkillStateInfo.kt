package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.PlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
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
     * 正在执行的 [PlayerSkillTick], 没有则返回 [SkillTick.empty].
     *
     * 同时也可以用来判断玩家是否正在施法状态.
     */
    val skillTick: PlayerSkillTick

    /**
     * 添加一个 [SingleTrigger],
     */
    fun addTrigger(trigger: SingleTrigger, context: SkillContext): SkillStateResult

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
    override val type: SkillStateInfo.Type,
    final override val skillTick: PlayerSkillTick
) : SkillStateInfo {
    protected var counter: Long = 0

    protected inner class TriggerConditionManager {
        fun isForbidden(trigger: SingleTrigger): Boolean {
            return skillTick.isForbidden(type, trigger)
        }

        fun isInterrupt(trigger: SingleTrigger): Boolean {
            return skillTick.isInterrupted(type, trigger)
        }
    }
}

/**
 * 表示玩家技能状态的空闲状态(无施法), 即玩家可以使用技能.
 */
class IdleStateInfo(
    private val state: PlayerSkillState,
) : AbstractSkillStateInfo(SkillStateInfo.Type.IDLE, SkillTick.empty()), KoinComponent {
    companion object {
        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val skillStateShower: SkillStateShower<Player> by inject()
    private val skillCastManager: SkillCastManager by inject()

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(3)

    private var castableSkill: Skill? = null

    override fun addTrigger(trigger: SingleTrigger, context: SkillContext): SkillStateResult {
        castableSkill = null
        // Sequence trigger skills
        if (trigger in SEQUENCE_GENERATION_TRIGGERS) {
            if (addSequenceSkills(trigger)) {
                skillStateShower.displaySuccess(currentSequence.readAll(), state.user)
                currentSequence.clear()
                return handleSkills(context)
            }
        }

        // Single trigger skills
        if (addSingleSkills(trigger)) {
            return handleSkills(context)
        }

        return SkillStateResult.SILENT_FAILURE
    }

    private fun addSequenceSkills(trigger: SingleTrigger): Boolean {
        val user = state.user
        val skillMap = user.skillMap
        // isFirstRightClickAndHasTrigger 的真值表:
        // currentSequence.isEmpty() | trigger == SingleTrigger.RIGHT_CLICK | skillMap.hasTrigger<SequenceTrigger>() -> isFirstRightClickAndHasTrigger
        // f | f | f -> f
        // f | f | t -> t
        // f | t | f -> f
        // f | t | t -> t
        // t | f | f -> f
        // t | f | t -> f
        // t | t | f -> f
        // t | t | t -> t
        // 可计算出最终表达式为: Result = skillMap.hasTrigger<SequenceTrigger>() && (!currentSequence.isEmpty() || trigger == SingleTrigger.RIGHT_CLICK)
        val isFirstRightClickAndHasTrigger = (!currentSequence.isEmpty() || trigger == SingleTrigger.RIGHT_CLICK) && skillMap.hasTrigger<SequenceTrigger>()

        if (isFirstRightClickAndHasTrigger) {
            // If the trigger is a sequence generation trigger, we should add it to the sequence
            currentSequence.write(trigger)
            val completeSequence = currentSequence.readAll()
            skillStateShower.displayProgress(completeSequence, user)

            if (currentSequence.isFull()) {
                val sequence = SequenceTrigger.of(completeSequence)
                val skillsOnSequence = skillMap.getSkill(sequence).firstOrNull()
                if (skillsOnSequence == null) {
                    currentSequence.clear()
                    skillStateShower.displayFailure(completeSequence, user)
                    return false
                }
                castableSkill = skillsOnSequence
                return true
            }
        }

        return false
    }

    private fun addSingleSkills(trigger: SingleTrigger): Boolean {
        val user = state.user
        val skillMap = user.skillMap
        val skills = skillMap.getSkill(trigger)
        if (skills.isEmpty()) {
            return false
        }
        castableSkill = skills.first()
        return true
    }

    private fun handleSkills(context: SkillContext): SkillStateResult {
        castableSkill ?: return SkillStateResult.SILENT_FAILURE
        val skillTick = skillCastManager.tryCast(castableSkill!!, context).skillTick as? PlayerSkillTick ?: return SkillStateResult.SILENT_FAILURE
        state.setInfo(CastPointStateInfo(state, skillTick))
        return SkillStateResult.CANCEL_EVENT
    }

    override fun tick() = Unit

    override fun interrupt() {
        currentSequence.clear()
        castableSkill = null
        skillStateShower.displayFailure(currentSequence.readAll(), state.user)
    }
}

/**
 * 表示玩家技能状态的前摇状态, 即玩家正在试图使用技能.
 */
class CastPointStateInfo(
    private val state: PlayerSkillState,
    skillTick: PlayerSkillTick,
) : AbstractSkillStateInfo(SkillStateInfo.Type.CAST_POINT, skillTick) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager()

    override fun addTrigger(trigger: SingleTrigger, context: SkillContext): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        if (triggerConditionManager.isInterrupt(trigger)) {
            interrupt()
            return SkillStateResult.SILENT_FAILURE
        }
        return SkillStateResult.SUCCESS
    }

    override fun tick() {
        val result = skillTick.tickCastPoint(counter)
        counter++
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
    skillTick: PlayerSkillTick,
) : AbstractSkillStateInfo(SkillStateInfo.Type.CAST, skillTick) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager()

    override fun addTrigger(trigger: SingleTrigger, context: SkillContext): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        if (triggerConditionManager.isInterrupt(trigger)) {
            interrupt()
            return SkillStateResult.SILENT_FAILURE
        }
        return SkillStateResult.SUCCESS
    }

    override fun tick() {
        val result = skillTick.tickCast(counter)
        counter++
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
    skillTick: PlayerSkillTick,
) : AbstractSkillStateInfo(SkillStateInfo.Type.BACKSWING, skillTick) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager()

    override fun addTrigger(trigger: SingleTrigger, context: SkillContext): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        if (triggerConditionManager.isInterrupt(trigger)) {
            interrupt()
        }
        return SkillStateResult.SUCCESS
    }

    override fun tick() {
        val result = skillTick.tickBackswing(counter)
        counter++
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
