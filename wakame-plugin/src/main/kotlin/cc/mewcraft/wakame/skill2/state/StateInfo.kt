package cc.mewcraft.wakame.skill2.state

import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.event.PlayerSkillStateChangeEvent
import cc.mewcraft.wakame.skill2.MechanicWorldInteraction
import cc.mewcraft.wakame.skill2.hasTrigger
import cc.mewcraft.wakame.skill2.state.display.StateDisplay
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

/**
 * 代表了一个玩家技能状态的信息.
 */
sealed interface StateInfo : Examinable {
    /**
     * 当前状态的类型.
     */
    val phase: StatePhase

    /**
     * 添加一个 [SingleTrigger],
     */
    fun addTrigger(trigger: SingleTrigger): SkillStateResult
}

sealed class AbstractStateInfo(
    val player: Player,
    override val phase: StatePhase,
    registerEvents: Boolean = true,
) : StateInfo {

    companion object : KoinComponent {
        private val mechanicWorldInteraction: MechanicWorldInteraction by inject()
    }

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

    protected fun interrupt() {
        mechanicWorldInteraction.interruptMechanicBy(player)
    }

    protected fun setNextState() {
        mechanicWorldInteraction.markNextState(player)
    }

    private fun registerTriggerEvents() {
        // FIXME: 检查可能出现的对象残留问题.
        val subscriptions = listOf(
            Events.subscribe(PlayerJumpEvent::class.java)
                .filter { it.player == player }
                .handler { event ->
                    val user = event.player.toUser()
                    val result = user.skillState.addTrigger(SingleTrigger.JUMP)
                    checkResult(result, event)
                },

            Events.subscribe(PlayerMoveEvent::class.java)
                .filter { it.player == player }
                .filter { it.from.blockX != it.to.blockX || it.from.blockY != it.to.blockY || it.from.blockZ != it.to.blockZ }
                .handler { event ->
                    val user = event.player.toUser()
                    val result = user.skillState.addTrigger(SingleTrigger.MOVE)
                    checkResult(result, event)
                },

            Events.subscribe(PlayerToggleSneakEvent::class.java)
                .filter { it.player == player }
                .handler { event ->
                    val user = event.player.toUser()
                    val result = user.skillState.addTrigger(SingleTrigger.SNEAK)
                    checkResult(result, event)
                }
        )

        EntitySubscriptionTerminator.newBuilder<PlayerSkillStateChangeEvent>()
            .terminatorEvent(PlayerSkillStateChangeEvent::class.java)
            .predicate { it.player == player }
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
            ExaminableProperty.of("type", phase)
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
    player: Player,
) : AbstractStateInfo(player, StatePhase.IDLE, false) {
    companion object : KoinComponent {
        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)

        private val stateDisplay: StateDisplay<Player> by inject()
    }

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(3)

    override fun addTrigger(trigger: SingleTrigger): SkillStateResult {
        val castTrigger = if (trigger == SingleTrigger.ATTACK) SingleTrigger.LEFT_CLICK else trigger

        // Sequence trigger skills
        if (castTrigger in SEQUENCE_GENERATION_TRIGGERS) {
            if (addSequenceSkills(castTrigger)) {
                stateDisplay.displaySuccess(currentSequence.readAll(),player)
                currentSequence.clear()
                setNextState()
                return SkillStateResult.CANCEL_EVENT
            }
        }

        // Single trigger skills
        if (addSingleSkills(castTrigger)) {
            setNextState()
            return SkillStateResult.CANCEL_EVENT
        }

        return SkillStateResult.SILENT_FAILURE
    }

    private fun addSequenceSkills(trigger: SingleTrigger): Boolean {
        val user = player.toUser()
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
            stateDisplay.displayProgress(completeSequence, player)

            if (currentSequence.isFull()) {
                val sequence = SequenceTrigger.of(completeSequence)
                val skillsOnSequence = skillMap.getSkill(sequence).firstOrNull()
                if (skillsOnSequence == null) {
                    currentSequence.clear()
                    stateDisplay.displayFailure(completeSequence, player)
                    return false
                }
                return true
            }
        }

        return false
    }

    private fun addSingleSkills(trigger: SingleTrigger): Boolean {
        val user = player.toUser()
        val skillMap = user.skillMap
        skillMap.getSkill(trigger).takeIf { it.isNotEmpty() } ?: return false
        return true
    }
}

/**
 * 表示玩家技能状态的前摇状态, 即玩家正在试图使用技能.
 */
class CastPointStateInfo(
    player: Player,
) : AbstractStateInfo(player, StatePhase.CAST_POINT) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager()

    override fun addTrigger(trigger: SingleTrigger): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        if (triggerConditionManager.isInterrupt(trigger)) {
            interrupt()
            return SkillStateResult.SILENT_FAILURE
        }
        return SkillStateResult.SUCCESS
    }
}

/**
 * 表示玩家技能状态的释放状态, 即玩家正在释放技能.
 */
class CastingStateInfo(
    player: Player,
) : AbstractStateInfo(player, StatePhase.CASTING) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager()

    override fun addTrigger(trigger: SingleTrigger): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        if (triggerConditionManager.isInterrupt(trigger)) {
            interrupt()
            return SkillStateResult.SILENT_FAILURE
        }
        return SkillStateResult.SUCCESS
    }
}

/**
 * 表示玩家技能状态的后摇状态, 即玩家释放技能后的状态.
 */
class BackswingStateInfo(
    player: Player,
) : AbstractStateInfo(player, StatePhase.BACKSWING) {
    private val triggerConditionManager: TriggerConditionManager = TriggerConditionManager()

    override fun addTrigger(trigger: SingleTrigger): SkillStateResult {
        if (triggerConditionManager.isForbidden(trigger)) {
            return SkillStateResult.CANCEL_EVENT
        }
        if (triggerConditionManager.isInterrupt(trigger)) {
            interrupt()
        }
        return SkillStateResult.SUCCESS
    }
}