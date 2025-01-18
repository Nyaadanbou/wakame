package cc.mewcraft.wakame.ability.state

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.event.PlayerManaCostEvent
import cc.mewcraft.wakame.event.PlayerNoEnoughManaEvent
import cc.mewcraft.wakame.ability.ManaCostPenalty
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.ManaCostPenalty
import cc.mewcraft.wakame.ability.abilityWorldInteraction
import cc.mewcraft.wakame.ability.state.display.StateDisplay
import cc.mewcraft.wakame.ability.trigger.SequenceTrigger
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.event.PlayerManaCostEvent
import cc.mewcraft.wakame.event.PlayerNoEnoughManaEvent
import cc.mewcraft.wakame.util.RingBuffer
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.event.Events
import cc.mewcraft.wakame.util.event.Subscription
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.*
import java.util.stream.Stream

/**
 * 代表了一个玩家技能状态的信息.
 */
sealed interface StateInfo : Examinable {
    /**
     * 添加一个 [SingleTrigger],
     */
    fun addTrigger(trigger: SingleTrigger): AbilityStateResult

    fun cleanup()
}

/**
 * 表示玩家技能状态的空闲状态(无施法), 即玩家可以使用技能.
 */
class PlayerStateInfo(
    player: Player,
) : StateInfo {
    private val weakPlayer: WeakReference<Player> = WeakReference(player)

    internal val player: Player
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

    companion object {
        private const val SEQUENCE_SIZE = 3

        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)

        private val stateDisplay: StateDisplay<Player> by Injector.inject()
        private val abilityWorldInteraction: AbilityWorldInteraction by Injector.inject()
    }

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(SEQUENCE_SIZE)
    private val mechanicName = "IdleResetMechanic-${player.uniqueId}"

    private val manaCostPenalties: MutableMap<String, ManaCostPenalty> = HashMap()

    private val idleResetMechanic: Mechanic = PlayerStateInfoMechanic(this)

    override fun addTrigger(trigger: SingleTrigger): AbilityStateResult {
        val castTrigger = if (trigger == SingleTrigger.ATTACK) SingleTrigger.LEFT_CLICK else trigger

        // Sequence trigger abilities
        if (castTrigger in SEQUENCE_GENERATION_TRIGGERS) {
            val sequenceAbility = trySequenceAbility(castTrigger)
            if (sequenceAbility != null) {
                stateDisplay.displaySuccess(currentSequence.readAll(), player)
                currentSequence.clear()
                WakameWorld.removeMechanic(mechanicName)
                markNextState(sequenceAbility)
                return AbilityStateResult.CANCEL_EVENT
            }
        }

        // Single trigger abilities
        val singleAbility = getAbilityByTrigger(castTrigger)
        if (singleAbility != null) {
            WakameWorld.removeMechanic(mechanicName)
            markNextState(singleAbility)
            return AbilityStateResult.CANCEL_EVENT
        }

        return AbilityStateResult.SILENT_FAILURE
    }

    private fun markNextState(ability: Ability) = abilityWorldInteraction {
        val abilityName = ability.key.asString()
        val costPenalty = penalizeManaCost(abilityName)
        player.setCostPenalty(abilityName, costPenalty)
        player.setNextState(ability)
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

    private fun trySequenceAbility(trigger: SingleTrigger): Ability? = abilityWorldInteraction {
        val triggerTypes = player.getAllActiveAbilityTriggers()
        // 第一个按下的是右键并且玩家有 Sequence 类型的 Trigger
        // isFirstRightClickAndHasTrigger 的真值表:
        // currentSequence.isEmpty() | trigger == SingleTrigger.RIGHT_CLICK | triggerTypes.any { it is SequenceTrigger } -> isFirstRightClickAndHasTrigger
        // f | f | f -> f
        // f | f | t -> t
        // f | t | f -> f
        // f | t | t -> t
        // t | f | f -> f
        // t | f | t -> f
        // t | t | f -> f
        // t | t | t -> t
        // 可计算出最终表达式为: Result = triggerTypes.any { it is SequenceTrigger } && (!currentSequence.isEmpty() || trigger == SingleTrigger.RIGHT_CLICK)
        val isFirstRightClickAndHasTrigger = (!currentSequence.isEmpty() || trigger == SingleTrigger.RIGHT_CLICK) && triggerTypes.any { it is SequenceTrigger }

        if (isFirstRightClickAndHasTrigger) {
            // If the trigger is a sequence generation trigger, we should add it to the sequence
            currentSequence.write(trigger)
            WakameWorld.createMechanic(mechanicName) { idleResetMechanic }
            val completeSequence = currentSequence.readAll()
            stateDisplay.displayProgress(completeSequence, player)

            if (currentSequence.isFull()) {
                val sequence = SequenceTrigger.fromSingleTriggers(completeSequence)
                val abilityOnSequence = sequence?.let { getAbilityByTrigger(it) }
                if (abilityOnSequence == null) {
                    currentSequence.clear()
                    stateDisplay.displayFailure(completeSequence, player)
                    return null
                }
                return abilityOnSequence
            }
        }

        return null
    }

    private fun getAbilityByTrigger(trigger: Trigger): Ability? = abilityWorldInteraction {
        val abilities = player.getAbilityBy(trigger)
        if (abilities.size > 1) {
            LOGGER.warn("Player ${player.name} has multiple abilities with the same trigger $trigger")
        }
        return abilities.firstOrNull()
    }

    fun clearSequence() {
        currentSequence.clear()
    }

    override fun cleanup() = abilityWorldInteraction {
        player.cleanupAbility()
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