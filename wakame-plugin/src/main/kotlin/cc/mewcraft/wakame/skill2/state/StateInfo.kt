package cc.mewcraft.wakame.skill2.state

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.SkillWorldInteraction
import cc.mewcraft.wakame.skill2.state.display.StateDisplay
import cc.mewcraft.wakame.skill2.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.util.RingBuffer
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference
import java.util.stream.Stream

/**
 * 代表了一个玩家技能状态的信息.
 */
sealed interface StateInfo : Examinable {
    /**
     * 添加一个 [SingleTrigger],
     */
    fun addTrigger(trigger: SingleTrigger): SkillStateResult
}

/**
 * 表示玩家技能状态的空闲状态(无施法), 即玩家可以使用技能.
 */
class IdleStateInfo(
    player: Player,
) : StateInfo {
    private val weakPlayer: WeakReference<Player> = WeakReference(player)

    private val player: Player
        get() = requireNotNull(weakPlayer.get())

    companion object : KoinComponent {
        private const val SEQUENCE_SIZE = 3

        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)

        private val stateDisplay: StateDisplay<Player> by inject()
        private val skillWorldInteraction: SkillWorldInteraction by inject()
        private val wakameWorld: WakameWorld by inject()
    }

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(SEQUENCE_SIZE)
    private val mechanicName = "IdleResetMechanic-${player.uniqueId}"

    private val idleResetMechanic: Mechanic = Mechanic(
        tick = { deltaTime, tickCount, componentMap ->
            if (tickCount <= 40) {
                return@Mechanic TickResult.CONTINUE_TICK
            }
            TickResult.ALL_DONE
        },
        onDisable = {
            currentSequence.clear()
        }
    )

    override fun addTrigger(trigger: SingleTrigger): SkillStateResult {
        val castTrigger = if (trigger == SingleTrigger.ATTACK) SingleTrigger.LEFT_CLICK else trigger

        // Sequence trigger skills
        if (castTrigger in SEQUENCE_GENERATION_TRIGGERS) {
            val sequenceSkills = trySequenceSkills(castTrigger)
            if (sequenceSkills.isNotEmpty()) {
                stateDisplay.displaySuccess(currentSequence.readAll(), player)
                currentSequence.clear()
                markNextState(sequenceSkills)
                wakameWorld.removeEntity(mechanicName)
                return SkillStateResult.CANCEL_EVENT
            }
        }

        // Single trigger skills
        val singleSkills = getSkillsByTrigger(castTrigger)
        if (singleSkills.isNotEmpty()) {
            markNextState(singleSkills)
            wakameWorld.removeEntity(mechanicName)
            return SkillStateResult.CANCEL_EVENT
        }

        return SkillStateResult.SILENT_FAILURE
    }

    private fun markNextState(skills: Collection<Skill>) {
        skillWorldInteraction.markNextState(player, skills)
    }

    private fun trySequenceSkills(trigger: SingleTrigger): Collection<Skill> {
        val triggerTypes = skillWorldInteraction.getAllActiveSkillTriggers(player)
        // 第一个按下的是右键并且 skillMap 内有 Sequence 类型的 Trigger
        // isFirstRightClickAndHasTrigger 的真值表:
        // currentSequence.isEmpty() | trigger == SingleTrigger.RIGHT_CLICK | skillMap.hasTriggerType<SequenceTrigger>() -> isFirstRightClickAndHasTrigger
        // f | f | f -> f
        // f | f | t -> t
        // f | t | f -> f
        // f | t | t -> t
        // t | f | f -> f
        // t | f | t -> f
        // t | t | f -> f
        // t | t | t -> t
        // 可计算出最终表达式为: Result = skillMap.hasTriggerType<SequenceTrigger>() && (!currentSequence.isEmpty() || trigger == SingleTrigger.RIGHT_CLICK)
        val isFirstRightClickAndHasTrigger = (!currentSequence.isEmpty() || trigger == SingleTrigger.RIGHT_CLICK) && triggerTypes.any { it is SequenceTrigger }

        if (isFirstRightClickAndHasTrigger) {
            // If the trigger is a sequence generation trigger, we should add it to the sequence
            currentSequence.write(trigger)
            wakameWorld.createMechanic(mechanicName) { idleResetMechanic }
            val completeSequence = currentSequence.readAll()
            stateDisplay.displayProgress(completeSequence, player)

            if (currentSequence.isFull()) {
                val sequence = SequenceTrigger.of(completeSequence)
                val skillsOnSequence = getSkillsByTrigger(sequence)
                if (skillsOnSequence.isEmpty()) {
                    currentSequence.clear()
                    stateDisplay.displayFailure(completeSequence, player)
                    return emptyList()
                }
                return skillsOnSequence
            }
        }

        return emptyList()
    }

    private fun getSkillsByTrigger(trigger: Trigger): Collection<Skill> {
        return skillWorldInteraction.getMechanicsBy(player, trigger)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("player", player),
            ExaminableProperty.of("currentSequence", currentSequence),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}