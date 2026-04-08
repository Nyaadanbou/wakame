package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseEntityContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import io.papermc.paper.datacomponent.DataComponentTypes

object DamageOnUseEntity : ItemBehavior {

    override fun handleUseEntity(context: UseEntityContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val damageOnUseEntity = itemstack.getProp(ItemPropTypes.DAMAGE_ON_USE_ENTITY) ?: return InteractionResult.PASS
        if (!(itemstack.hasData(DataComponentTypes.MAX_DAMAGE) && itemstack.hasData(DataComponentTypes.DAMAGE))) {
            itemstack.setData(DataComponentTypes.MAX_DAMAGE, damageOnUseEntity)
            itemstack.setData(DataComponentTypes.DAMAGE, 0)
        }
        itemstack.damage(damageOnUseEntity, player)
        return InteractionResult.PASS
    }
}