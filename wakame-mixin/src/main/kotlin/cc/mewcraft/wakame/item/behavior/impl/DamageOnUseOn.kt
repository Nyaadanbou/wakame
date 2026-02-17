package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseOnContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object DamageOnUseOn : ItemBehavior {

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val damageOnUseOn = itemstack.getProp(ItemPropTypes.DAMAGE_ON_USE_ON) ?: return InteractionResult.PASS
        itemstack.damage(damageOnUseOn, player)
        return InteractionResult.PASS
    }
}