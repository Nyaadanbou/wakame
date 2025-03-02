package cc.mewcraft.wakame.ability.state

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.ManaCostPenalty
import cc.mewcraft.wakame.ability.playerAbilityWorldInteraction
import cc.mewcraft.wakame.ability.state.display.PlayerComboInfoDisplay
import cc.mewcraft.wakame.ability.trigger.SequenceTrigger
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.event.bukkit.PlayerManaCostEvent
import cc.mewcraft.wakame.event.bukkit.PlayerNoEnoughManaEvent
import cc.mewcraft.wakame.util.RingBuffer
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.event.Events
import cc.mewcraft.wakame.util.event.Subscription
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import java.lang.ref.WeakReference
import java.util.stream.Stream

/**
 * 玩家打出 Combo 的具体内部实现.
 */
class PlayerComboInfo(
    player: Player,
) : Examinable {
    private val weakPlayer: WeakReference<Player> = WeakReference(player)

    internal val player: Player
        get() = requireNotNull(weakPlayer.get())

    private val manaNoEnoughSubscription: Subscription = Events.subscribe(PlayerNoEnoughManaEvent::class.java)
        .filter { it.player == player }
        .handler { event ->
            if (event.player == player) {
                PlayerComboInfoDisplay.displayNoEnoughMana(player)
            }
        }

    private val manaCostSubscription: Subscription = Events.subscribe(PlayerManaCostEvent::class.java)
        .filter { it.player == player }
        .handler { event ->
            if (event.player == player) {
                PlayerComboInfoDisplay.displayManaCost(event.manaCost, player)
            }
        }

    companion object {
        private const val SEQUENCE_SIZE = 3

        private val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> =
            listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)
    }

    private val currentSequence: RingBuffer<SingleTrigger> = RingBuffer(SEQUENCE_SIZE)
    private val manaCostPenalties: MutableMap<String, ManaCostPenalty> = HashMap()

    private val mechanicName = "PlayerStateInfoResetMechanic-${player.uniqueId}"
    private val resetMechanic: Mechanic = PlayerStateInfoResetMechanic(this)

    fun addTrigger(trigger: SingleTrigger): PlayerComboResult {
        val castTrigger = if (trigger == SingleTrigger.ATTACK) SingleTrigger.LEFT_CLICK else trigger

        // 尝试找到使用当前触发器能够触发的技能
        val singleAbility = getAbilityByTrigger(castTrigger)
        if (singleAbility != null) {
            WakameWorld.removeMechanic(mechanicName)
            markNextState(singleAbility)
            return PlayerComboResult.CANCEL_EVENT
        }

        // 尝试找到使用当前 combo 能够触发的技能
        if (castTrigger in SEQUENCE_GENERATION_TRIGGERS) {
            val sequenceAbility = trySequenceAbility(castTrigger)
            if (sequenceAbility != null) {
                PlayerComboInfoDisplay.displaySuccess(currentSequence.readAll(), player)
                WakameWorld.removeMechanic(mechanicName)
                markNextState(sequenceAbility)
                return PlayerComboResult.CANCEL_EVENT
            }
        }

        return PlayerComboResult.SILENT_FAILURE
    }

    private fun markNextState(ability: Ability) = playerAbilityWorldInteraction {
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

    private fun trySequenceAbility(trigger: SingleTrigger): Ability? = playerAbilityWorldInteraction {
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
            WakameWorld.addMechanic(mechanicName, resetMechanic)
            val completeSequence = currentSequence.readAll()
            PlayerComboInfoDisplay.displayProgress(completeSequence, player)

            if (currentSequence.isFull()) {
                val sequence = SequenceTrigger.fromSingleTriggers(completeSequence)
                val abilityOnSequence = sequence?.let { getAbilityByTrigger(it) }
                // 如果成功，则清除当前序列
                currentSequence.clear()
                if (abilityOnSequence == null) {
                    PlayerComboInfoDisplay.displayFailure(completeSequence, player)
                    return null
                }
                return abilityOnSequence
            }
        }

        return null
    }

    private fun getAbilityByTrigger(trigger: Trigger): Ability? = playerAbilityWorldInteraction {
        val abilities = player.getAbilityBy(trigger)
        if (abilities.size > 1) {
            LOGGER.warn("Player ${player.name} has multiple abilities with the same trigger $trigger")
        }
        return abilities.firstOrNull()
    }

    fun clearSequence() {
        currentSequence.clear()
    }

    fun cleanup() = playerAbilityWorldInteraction {
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