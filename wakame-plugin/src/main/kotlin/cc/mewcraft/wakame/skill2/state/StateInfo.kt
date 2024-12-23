package cc.mewcraft.wakame.skill2.state

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.event.PlayerManaCostEvent
import cc.mewcraft.wakame.event.PlayerNoEnoughManaEvent
import cc.mewcraft.wakame.skill2.ManaCostPenalty
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.SkillWorldInteraction
import cc.mewcraft.wakame.skill2.state.display.StateDisplay
import cc.mewcraft.wakame.skill2.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.util.RingBuffer
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.Events
import me.lucko.helper.event.Subscription
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
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

    fun cleanup()
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

    private val manaNoEnoughSubscription: Subscription = Events.subscribe(PlayerNoEnoughManaEvent::class.java)
        .filter { it.player == player }
        .handler { event ->
            if (event.player == player) {
                stateDisplay.displayNoEnoughMana(player)
            }
        }

    private val manaCostSubscription: Subscription = Events.subscribe(PlayerManaCostEvent::class.java)
        .filter { it.player == player }
        .handler { event ->
            if (event.player == player) {
                stateDisplay.displayManaCost(event.manaCost, player)
            }
        }

    companion object : KoinComponent {
        private const val SEQUENCE_SIZE = 3

        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)

        private val logger: Logger by inject()
        private val stateDisplay: StateDisplay<Player> by inject()
        private val skillWorldInteraction: SkillWorldInteraction by inject()
        private val wakameWorld: WakameWorld by inject()
    }

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(SEQUENCE_SIZE)
    private val mechanicName = "IdleResetMechanic-${player.uniqueId}"

    private val manaCostPenalties: MutableMap<String, ManaCostPenalty> = HashMap()

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
            val sequenceSkill = trySequenceSkills(castTrigger)
            if (sequenceSkill != null) {
                stateDisplay.displaySuccess(currentSequence.readAll(), player)
                currentSequence.clear()
                wakameWorld.removeEntity(mechanicName)
                markNextState(sequenceSkill)
                return SkillStateResult.CANCEL_EVENT
            }
        }

        // Single trigger skills
        val singleSkill = getSkillsByTrigger(castTrigger)
        if (singleSkill != null) {
            wakameWorld.removeEntity(mechanicName)
            markNextState(singleSkill)
            return SkillStateResult.CANCEL_EVENT
        }

        return SkillStateResult.SILENT_FAILURE
    }

    private fun markNextState(skill: Skill) {
        val skillName = skill.key.asString()
        val costPenalty = penalizeManaCost(skillName)
        skillWorldInteraction.setCostPenalty(player, skillName, costPenalty)
        skillWorldInteraction.setNextState(player, skill)
    }

    private fun penalizeManaCost(identifier: String): ManaCostPenalty {
        val penalty = manaCostPenalties.getOrPut(identifier) { ManaCostPenalty() }
        if (penalty.cooldown.testSilently()) {
            penalty.penaltyCount = 1
        } else {
            penalty.penaltyCount++
        }
        return penalty
    }

    private fun trySequenceSkills(trigger: SingleTrigger): Skill? {
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
                val skillOnSequence = getSkillsByTrigger(sequence)
                if (skillOnSequence == null) {
                    currentSequence.clear()
                    stateDisplay.displayFailure(completeSequence, player)
                    return null
                }
                return skillOnSequence
            }
        }

        return null
    }

    private fun getSkillsByTrigger(trigger: Trigger): Skill? {
        val skills = skillWorldInteraction.getMechanicsBy(player, trigger)
        if (skills.size > 1) {
            logger.warn("Player ${player.name} has multiple skills with the same trigger $trigger")
        }
        return skills.firstOrNull()
    }

    override fun cleanup() {
        manaNoEnoughSubscription.close()
        manaCostSubscription.close()
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