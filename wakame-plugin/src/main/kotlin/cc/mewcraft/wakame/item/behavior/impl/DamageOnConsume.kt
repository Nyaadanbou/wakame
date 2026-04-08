package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import io.papermc.paper.datacomponent.DataComponentTypes

object DamageOnConsume : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        val itemstack = context.itemstack
        val damageOnConsume = itemstack.getProp(ItemPropTypes.DAMAGE_ON_CONSUME) ?: return BehaviorResult.PASS
        val clone = itemstack.clone()
        if (!(clone.hasData(DataComponentTypes.MAX_DAMAGE) && clone.hasData(DataComponentTypes.DAMAGE))) {
            clone.setData(DataComponentTypes.MAX_DAMAGE, damageOnConsume)
            clone.setData(DataComponentTypes.DAMAGE, 0)
        }
        context.modifyReplacement(clone.damage(damageOnConsume, player))
        return BehaviorResult.PASS
    }
}