package cc.mewcraft.wakame.ability2.combo

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.ability2.AbilityEcsBridge
import cc.mewcraft.wakame.ability2.ManaCostPenalty
import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.combo.display.PlayerComboInfoDisplay
import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.ManaCost
import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.trigger.AbilitySequenceTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.event.bukkit.PlayerManaConsumeEvent
import cc.mewcraft.wakame.event.bukkit.PlayerNotEnoughManaEvent
import cc.mewcraft.wakame.util.RingBuffer
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.event.Events
import cc.mewcraft.wakame.util.event.Subscription
import cc.mewcraft.wakame.util.runTaskLater
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.stream.Stream

/**
 * 用于存储玩家搓连招的状态以及将信息显示给玩家.
 */
class PlayerComboInfo(
    private val uniqueId: UUID,
) : Examinable {
    private val player: Player
        get() = requireNotNull(SERVER.getPlayer(uniqueId))

    private val manaNoEnoughSubscription: Subscription = Events.subscribe(PlayerNotEnoughManaEvent::class.java)
        .filter { it.player == player }
        .handler { PlayerComboInfoDisplay.displayNotEnoughMana(player) }

    private val manaCostSubscription: Subscription = Events.subscribe(PlayerManaConsumeEvent::class.java)
        .filter { it.player == player }
        .handler { PlayerComboInfoDisplay.displayManaCost(it.amount, player) }

    companion object {
        private const val SEQUENCE_SIZE = 3
        private val SEQUENCE_GENERATION_TRIGGERS = listOf(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK)
    }

    private val comboSequence: RingBuffer<AbilitySingleTrigger> = RingBuffer(SEQUENCE_SIZE)
    private val manaCostPenalties: MutableMap<AbilityMetaType<*>, ManaCostPenalty> = HashMap()
    private var resetTask: BukkitTask? = null

    fun handleTrigger(trigger: AbilitySingleTrigger) {
        val singleAbility = player.getAbilityMetasBy(trigger)
        if (singleAbility.isNotEmpty()) {
            markNextState(singleAbility)
            return
        }

        if (trigger in SEQUENCE_GENERATION_TRIGGERS) {
            val sequenceAbility = trySequenceAbility(trigger)
            if (sequenceAbility.isNotEmpty()) {
                markNextState(sequenceAbility)
            }
        }
    }

    private fun markNextState(abilities: List<AbilityMetaType<*>>) {
        abilities.forEach { abilityMeta ->
            val costPenalty = penalizeManaCost(abilityMeta)
            player.updateAbilityState(abilityMeta, costPenalty)
        }
    }

    private fun penalizeManaCost(abilityMetaType: AbilityMetaType<*>): ManaCostPenalty {
        return manaCostPenalties.getOrPut(abilityMetaType) { ManaCostPenalty() }.apply {
            penaltyCount = if (resetCooldown.testSilently()) 1 else penaltyCount + 1
        }
    }

    private fun trySequenceAbility(trigger: AbilitySingleTrigger): List<AbilityMetaType<*>> {
        val hasSequenceTrigger = player.getAllActiveTriggers().any { it is AbilitySequenceTrigger }
        val isValidSequenceStart = !comboSequence.isEmpty() || trigger == AbilitySingleTrigger.RIGHT_CLICK

        if (hasSequenceTrigger && isValidSequenceStart) {
            comboSequence.write(trigger)
            scheduleResetTask()
            PlayerComboInfoDisplay.displayProgress(comboSequence.readAll(), player)

            if (comboSequence.isFull()) {
                val sequence = AbilitySequenceTrigger.of(comboSequence.readAll())
                val abilities = player.getAbilityMetasBy(sequence)
                if (abilities.isEmpty()) {
                    PlayerComboInfoDisplay.displayFailure(comboSequence.readAll(), player)
                } else {
                    PlayerComboInfoDisplay.displaySuccess(comboSequence.readAll(), player)
                }
                clearSequence()
                return abilities
            }
        }
        return emptyList()
    }

    private fun scheduleResetTask() {
        resetTask?.cancel()
        resetTask = runTaskLater(40) { clearSequence() }
    }

    private fun Player.getAbilityMetasBy(trigger: AbilityTrigger): List<AbilityMetaType<*>> {
        return AbilityEcsBridge.getPlayerAllSingleAbilities(this)
            .filter { it.trigger == trigger }
            .map { it.metaType }
    }

    private fun Player.getAllActiveTriggers(): Set<AbilityTrigger> {
        return AbilityEcsBridge.getPlayerAllSingleAbilities(this)
            .mapNotNull { it.trigger }
            .toSet()
    }

    private fun Player.updateAbilityState(abilityMetaType: AbilityMetaType<*>, penalty: ManaCostPenalty) {
        Families.ABILITY.forEach { entity ->
            if (entity[CastBy].entityOrPlayer() != this@updateAbilityState)
                return@forEach
            if (entity[Ability].metaType != abilityMetaType)
                return@forEach
            if (entity[Ability].phase == StatePhase.IDLE) {
                entity[Ability].phase = StatePhase.CAST_POINT
            }
            entity[ManaCost].penalty = penalty
        }
    }

    fun clearSequence() {
        comboSequence.clear()
    }

    fun cleanup() {
        manaNoEnoughSubscription.close()
        manaCostSubscription.close()
        resetTask?.cancel()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty?> {
        return Stream.of(
            ExaminableProperty.of("player", player),
            ExaminableProperty.of("comboSequence", comboSequence),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}