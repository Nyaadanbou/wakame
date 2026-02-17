package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseOnContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import io.papermc.paper.datacomponent.DataComponentTypes

object DamageOnUseOn : ItemBehavior {

    override fun handleUseOn(context: UseOnContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val damageOnUseOn = itemstack.getProp(ItemPropTypes.DAMAGE_ON_USE_ON) ?: return InteractionResult.PASS
        if (!(itemstack.hasData(DataComponentTypes.MAX_DAMAGE) && itemstack.hasData(DataComponentTypes.DAMAGE))) {
            itemstack.setData(DataComponentTypes.MAX_DAMAGE, damageOnUseOn)
            itemstack.setData(DataComponentTypes.DAMAGE, 0)
        }
        itemstack.damage(damageOnUseOn, player)
        return InteractionResult.PASS
    }
}