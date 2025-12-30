package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.*
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.CastableTrigger
import cc.mewcraft.wakame.item.property.impl.GenericCastableTrigger
import cc.mewcraft.wakame.item.property.impl.SequenceCastableTrigger
import cc.mewcraft.wakame.item.property.impl.SpecialCastableTrigger
import cc.mewcraft.wakame.item.tryCastSkill
import org.bukkit.entity.Player
import cc.mewcraft.wakame.item.property.impl.Castable as CastableProp

object Castable : SimpleInteract {

    // Implements the following triggers in castable:
    // - generic/right_click
    // - sequence/1
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, GenericCastableTrigger.RIGHT_CLICK)
        }

        return InteractionResult.PASS
    }

    // Implements the following triggers in castable:
    // - generic/left_click
    // - sequence/0
    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return InteractionResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, GenericCastableTrigger.LEFT_CLICK)
        }

        return InteractionResult.PASS
    }

    // Implements the following triggers in castable:
    // - special/on_consume
    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val castable = context.itemstack.getProp(ItemPropTypes.CASTABLE) ?: return BehaviorResult.PASS
        val player = context.player

        castable.values.forEach { entry ->
            handleTrigger(player, entry, SpecialCastableTrigger.ON_CONSUME)
        }

        return BehaviorResult.PASS
    }

    //region Logic for handling trigger-based casting

    private fun handleTrigger(player: Player, castable: CastableProp, expected: CastableTrigger) {
        val trigger = castable.trigger.unwrap()
        when (trigger) {
            expected -> {
                tryCastSkill(player, castable)
            }

            is SequenceCastableTrigger -> {
                // TODO Accumulate combo
                //   Check whether there are 3 accumulated combos
                //   If there are 3, check whether they match the current sequence; if matched, cast; otherwise, clear
            }

            else -> {}
        }
    }

    //endregion
}