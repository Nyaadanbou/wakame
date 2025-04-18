package cc.mewcraft.wakame.ability2.combo

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

    private val manaNoEnoughSubscription: Subscription = Events.subscribe(PlayerNotEnoughManaEvent::class.java)
        .filter { it.player == player }
        .handler { event ->
            if (event.player == player) {
                PlayerComboInfoDisplay.displayNotEnoughMana(player)
            }
        }

    private val manaCostSubscription: Subscription = Events.subscribe(PlayerManaConsumeEvent::class.java)
        .filter { it.player == player }
        .handler { event ->
            if (event.player == player) {
                PlayerComboInfoDisplay.displayManaCost(event.amount, player)
            }
        }

    companion object {
        private const val SEQUENCE_SIZE = 3

        private val SEQUENCE_GENERATION_TRIGGERS: List<AbilitySingleTrigger> =
            listOf(AbilitySingleTrigger.LEFT_CLICK, AbilitySingleTrigger.RIGHT_CLICK)
    }

    private val currentSequence: RingBuffer<AbilitySingleTrigger> = RingBuffer(SEQUENCE_SIZE)
    private val manaCostPenalties: MutableMap<AbilityMetaType<*>, ManaCostPenalty> = HashMap()

    private var resetTask: BukkitTask? = null

    private fun createResetTask(): BukkitTask {
        return runTaskLater(40) { clearSequence() }
    }

    private fun cancelTask() {
        resetTask?.cancel()
        resetTask = null
    }

    fun addTrigger(trigger: AbilitySingleTrigger): PlayerComboResult {
        // 尝试找到使用当前触发器能够触发的技能
        val singleAbility = player.getAbilityMetasBy(trigger)
        if (singleAbility.isNotEmpty()) {
            markNextState(singleAbility)
            return PlayerComboResult.CANCEL_EVENT
        }

        // 尝试找到使用当前 combo 能够触发的技能
        if (trigger in SEQUENCE_GENERATION_TRIGGERS) {
            val sequenceAbility = trySequenceAbility(trigger)
            if (sequenceAbility.isNotEmpty()) {
                markNextState(sequenceAbility)
                return PlayerComboResult.CANCEL_EVENT
            }
        }

        return PlayerComboResult.SILENT_FAILURE
    }

    private fun markNextState(abilities: List<AbilityMetaType<*>>) {
        for (abilityMeta in abilities) {
            val costPenalty = penalizeManaCost(abilityMeta)
            player.setCostPenalty(abilityMeta, costPenalty)
            player.setNextState(abilityMeta)
        }
    }

    private fun penalizeManaCost(abilityMetaType: AbilityMetaType<*>): ManaCostPenalty {
        val penalty = manaCostPenalties.getOrPut(abilityMetaType) { ManaCostPenalty() }
        if (penalty.resetCooldown.testSilently()) {
            penalty.penaltyCount = 1
        } else {
            penalty.penaltyCount++
        }
        return penalty
    }

    private fun trySequenceAbility(trigger: AbilitySingleTrigger): List<AbilityMetaType<*>> {
        val triggerTypes = player.getAllActiveTriggers()
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
        val isFirstRightClickAndHasTrigger = (!currentSequence.isEmpty() || trigger == AbilitySingleTrigger.RIGHT_CLICK) && triggerTypes.any { it is AbilitySequenceTrigger }

        if (isFirstRightClickAndHasTrigger) {
            // If the trigger is a sequence generation trigger, we should add it to the sequence
            currentSequence.write(trigger)
            cancelTask()
            resetTask = createResetTask()
            val completeSequence = currentSequence.readAll()
            PlayerComboInfoDisplay.displayProgress(completeSequence, player)

            if (currentSequence.isFull()) {
                val sequence = AbilitySequenceTrigger.of(completeSequence)
                val abilityOnSequence = player.getAbilityMetasBy(sequence)
                // 如果成功，则清除当前序列
                PlayerComboInfoDisplay.displaySuccess(currentSequence.readAll(), player)
                clearSequence()
                cancelTask()
                if (abilityOnSequence.isEmpty()) {
                    PlayerComboInfoDisplay.displayFailure(completeSequence, player)
                    return emptyList()
                }
                return abilityOnSequence
            }
        }

        return emptyList()
    }

    private fun Player.getAbilityMetasBy(trigger: AbilityTrigger): List<AbilityMetaType<*>> {
        return AbilityEcsBridge.getPlayerAllSingleAbilities(this).filter { it.trigger == trigger }.map { it.metaType }
    }

    private fun Player.getAllActiveTriggers(): Set<AbilityTrigger> {
        return AbilityEcsBridge.getPlayerAllSingleAbilities(this).mapNotNull { it.trigger }.toSet()
    }

    private fun Player.setNextState(abilityMetaType: AbilityMetaType<*>) {
        Families.ABILITY.forEach { entity ->
            if (entity[CastBy].entityOrPlayer() != this@setNextState)
                return@forEach
            if (entity[Ability].phase != StatePhase.IDLE)
                return@forEach // 只有在 IDLE 状态下才能进行下一个状态的标记.
            if (entity[Ability].metaType != abilityMetaType)
                return@forEach
            entity[Ability].isMarkNextState = true
        }
    }

    private fun Player.setCostPenalty(abilityMetaType: AbilityMetaType<*>, penalty: ManaCostPenalty) {
        Families.MANA_COSTING_ABILITY.forEach { entity ->
            if (entity[CastBy].entityOrPlayer() != this@setCostPenalty)
                return@forEach
            if (entity[Ability].metaType != abilityMetaType)
                return@forEach
            entity[ManaCost].penalty = penalty
        }
    }

    fun clearSequence() {
        currentSequence.clear()
    }

    fun cleanup() {
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