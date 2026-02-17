package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object DamageOnUse : ItemBehavior {

    override fun handleUse(context: UseContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val damageOnUse = itemstack.getProp(ItemPropTypes.DAMAGE_ON_USE) ?: return InteractionResult.PASS
        itemstack.damage(damageOnUse, player)
        return InteractionResult.PASS
    }
}