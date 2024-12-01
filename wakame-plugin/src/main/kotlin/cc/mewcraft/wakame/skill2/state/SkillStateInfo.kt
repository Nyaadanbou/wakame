package cc.mewcraft.wakame.skill2.state

import cc.mewcraft.wakame.event.PlayerSkillStateChangeEvent
import cc.mewcraft.wakame.item.takeIfNeko
import cc.mewcraft.wakame.item.toNekoStack
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.SkillCastManager
import cc.mewcraft.wakame.skill2.result.SkillResult
import cc.mewcraft.wakame.skill2.character.TargetAdapter
import cc.mewcraft.wakame.skill2.character.toComposite
import cc.mewcraft.wakame.skill2.context.ImmutableSkillContext
import cc.mewcraft.wakame.skill2.context.SkillContext
import cc.mewcraft.wakame.skill2.hasTrigger
import cc.mewcraft.wakame.skill2.state.display.StateDisplay
import cc.mewcraft.wakame.skill2.state.exception.IllegalSkillStateException
import cc.mewcraft.wakame.skill2.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.util.EntitySubscriptionTerminator
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.RingBuffer
import cc.mewcraft.wakame.util.toSimpleString
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import me.lucko.helper.Events
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.stream.Stream
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

/**
 * 代表了一个玩家技能状态的信息.
 */
sealed interface SkillStateInfo : Examinable {
    /**
     * 当前状态的类型.
     */
    val type: Type

    /**
     * 正在执行的 [SkillResult], 没有则返回空的 [SkillResult].
     *
     * 同时也可以用来判断玩家是否正在施法状态.
     */
    val skillResult: SkillResult<*>

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
         * 只能拿到空的 [SkillResult] 实例.
         */
        IDLE,
        CAST_POINT,
        CASTING,
        BACKSWING,
        ;
    }
}

sealed class AbstractSkillStateInfo(
    val state: PlayerSkillState,
    final override val skillResult: SkillResult<*>,
    override val type: SkillStateInfo.Type,
    registerEvents: Boolean = true,
) : SkillStateInfo {

    init {
        if (registerEvents) {
            registerTriggerEvents()
        }
    }

    protected inner class TriggerConditionManager {
        fun isForbidden(trigger: SingleTrigger): Boolean {
//            return skillResult.isForbidden(type, trigger)
            return false
        }

        fun isInterrupt(trigger: SingleTrigger): Boolean {
//            return skillResult.isInterrupted(type, trigger)
            return false
        }
    }

    override fun tick() {
        try {
            executeSkill()
        } catch (t: Throwable) {
            val skillKClass = skillResult.context::class
            val skillName = skillKClass.superclasses.first { it.isSubclassOf(Skill::class) }.simpleName ?: skillKClass.simpleName
            throw IllegalSkillStateException("在执行 $skillName 技能时发生了异常", t)
        }
    }

    protected abstract fun executeSkill()
    protected abstract fun setNextState()

    private fun registerTriggerEvents() {
        // FIXME: 检查可能出现的对象残留问题.
        val subscriptions = listOf(
            Events.subscribe(PlayerJumpEvent::class.java)
                .filter { it.player == state.user.player }
                .handler { event ->
                    val user = event.player.toUser()
                    val itemStack = user.player.inventory.itemInMainHand.takeIfNeko()
                    val nekoStack = itemStack?.toNekoStack
                    val result = user.skillState.addTrigger(SingleTrigger.JUMP, ImmutableSkillContext(CasterAdapter.adapt(user).toComposite(), TargetAdapter.adapt(user), nekoStack))
                    checkResult(result, event)
                },

            Events.subscribe(PlayerMoveEvent::class.java)
                .filter { it.player == state.user.player }
                .filter { it.from.blockX != it.to.blockX || it.from.blockY != it.to.blockY || it.from.blockZ != it.to.blockZ }
                .handler { event ->
                    val user = event.player.toUser()
                    val itemStack = event.player.inventory.itemInMainHand.takeIfNeko()
                    val nekoStack = itemStack?.toNekoStack
                    val result = user.skillState.addTrigger(SingleTrigger.MOVE, ImmutableSkillContext(CasterAdapter.adapt(user).toComposite(), TargetAdapter.adapt(user), nekoStack))
                    checkResult(result, event)
                },

            Events.subscribe(PlayerToggleSneakEvent::class.java)
                .filter { it.player == state.user.player }
                .handler { event ->
                    val user = event.player.toUser()
                    val itemStack = event.player.inventory.itemInMainHand.takeIfNeko()
                    val nekoStack = itemStack?.toNekoStack
                    val result = user.skillState.addTrigger(SingleTrigger.SNEAK, ImmutableSkillContext(CasterAdapter.adapt(user).toComposite(), TargetAdapter.adapt(user), nekoStack))
                    checkResult(result, event)
                }
        )

        EntitySubscriptionTerminator.newBuilder<PlayerSkillStateChangeEvent>()
            .terminatorEvent(PlayerSkillStateChangeEvent::class.java)
            .predicate { it.player == state.user.player }
            .addSubscriptions(subscriptions)
            .build()
            .startListen()
    }

    private fun checkResult(result: SkillStateResult, event: Cancellable) {
        if (result == SkillStateResult.CANCEL_EVENT) {
            event.isCancelled = true
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("type", type)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 表示玩家技能状态的空闲状态(无施法), 即玩家可以使用技能.
 */
class IdleStateInfo(
    state: PlayerSkillState,
) : AbstractSkillStateInfo(state, SkillResult(), SkillStateInfo.Type.IDLE, false), KoinComponent {
    companion object {
        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val stateDisplay: StateDisplay<Player> by inject()
    private val skillCastManager: SkillCastManager by inject()

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(3)
    private var sequenceCounter: Long = 0

    private var castableSkill: Skill? = null

    override fun addTrigger(trigger: SingleTrigger, context: SkillContext): SkillStateResult {
        castableSkill = null
        val castTrigger = if (trigger == SingleTrigger.ATTACK) SingleTrigger.LEFT_CLICK else trigger

        // Sequence trigger skills
        if (castTrigger in SEQUENCE_GENERATION_TRIGGERS) {
            if (addSequenceSkills(castTrigger)) {
                stateDisplay.displaySuccess(currentSequence.readAll(), state.user)
                currentSequence.clear()
                return handleSkills(context)
            }
        }

        // Single trigger skills
        if (addSingleSkills(castTrigger)) {
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
            stateDisplay.displayProgress(completeSequence, user)

            if (currentSequence.isFull()) {
                val sequence = SequenceTrigger.of(completeSequence)
                val skillsOnSequence = skillMap.getSkill(sequence).firstOrNull()
                if (skillsOnSequence == null) {
                    currentSequence.clear()
                    stateDisplay.displayFailure(completeSequence, user)
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
        castableSkill = skills.firstOrNull() ?: return false
        return true
    }

    private fun handleSkills(context: SkillContext): SkillStateResult {
        val castableSkill = castableSkill ?: return SkillStateResult.SILENT_FAILURE
        val skillTick = skillCastManager.tryCast(castableSkill, context).skillTick as? PlayerSkillTick ?: return SkillStateResult.SILENT_FAILURE
        state.setInfo(CastPointStateInfo(state, skillTick))
        return SkillStateResult.CANCEL_EVENT
    }

    override fun tick() {
        // Invalidate the sequence if it's not empty
        if (!currentSequence.isEmpty()) {
            sequenceCounter++
            if (sequenceCounter >= 30) {
                stateDisplay.displayFailure(currentSequence.readAll(), state.user)
                currentSequence.clear()
            }
        } else {
            sequenceCounter = 0
        }
    }

    override fun executeSkill() {
        throw UnsupportedOperationException("IdleStateInfo does not support tickSkill")
    }

    override fun setNextState() {
        throw UnsupportedOperationException("IdleStateInfo does not support setNextState")
    }

    override fun interrupt() {
        currentSequence.clear()
        castableSkill = null
    }
}

/**
 * 表示玩家技能状态的前摇状态, 即玩家正在试图使用技能.
 */
class CastPointStateInfo(
    state: PlayerSkillState,
    skillResult: SkillResult<*>,
) : AbstractSkillStateInfo(state, skillResult, SkillStateInfo.Type.CAST_POINT) {
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

    override fun executeSkill() {
        return skillResult.tickCastPoint()
    }

    override fun setNextState() {
        state.setInfo(CastingStateInfo(state, skillResult))
    }

    override fun interrupt() {
        state.setInfo(IdleStateInfo(state))
    }
}

/**
 * 表示玩家技能状态的释放状态, 即玩家正在释放技能.
 */
class CastingStateInfo(
    state: PlayerSkillState,
    skillResult: SkillResult<*>,
) : AbstractSkillStateInfo(state, skillResult, SkillStateInfo.Type.CASTING) {
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

    override fun executeSkill() {
        return skillResult.tickCast()
    }

    override fun setNextState() {
        state.setInfo(BackswingStateInfo(state, skillResult))
    }

    override fun interrupt() {
        state.setInfo(IdleStateInfo(state))
    }
}

/**
 * 表示玩家技能状态的后摇状态, 即玩家释放技能后的状态.
 */
class BackswingStateInfo(
    state: PlayerSkillState,
    skillResult: SkillResult<*>,
) : AbstractSkillStateInfo(state, skillResult, SkillStateInfo.Type.BACKSWING) {
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

    override fun executeSkill() {
        return skillResult.tickBackswing()
    }

    override fun setNextState() {
        state.setInfo(IdleStateInfo(state))
    }

    override fun interrupt() {
        state.setInfo(IdleStateInfo(state))
    }
}