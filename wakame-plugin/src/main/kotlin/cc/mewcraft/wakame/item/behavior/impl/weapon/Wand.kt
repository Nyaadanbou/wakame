package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.weapon.checkTwoHandedRequirement
import cc.mewcraft.wakame.item.property.impl.weapon.handleTwoHandedFailure

object Wand : Weapon {
    override fun handleSimpleAttack(context: AttackContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        val wand = itemstack.getProp(ItemPropTypes.WAND) ?: return InteractionResult.PASS

        // 检查双手持握
        if (!wand.checkTwoHandedRequirement(player)) {
            wand.handleTwoHandedFailure(player)
            return InteractionResult.FAIL
        }

        return InteractionResult.PASS
    }

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val itemstack = context.itemstack
        val player = context.player
        val wand = itemstack.getProp(ItemPropTypes.WAND) ?: return InteractionResult.PASS

        // 检查双手持握
        if (!wand.checkTwoHandedRequirement(player)) {
            wand.handleTwoHandedFailure(player)
            return InteractionResult.FAIL
        }

        return InteractionResult.PASS
    }
}