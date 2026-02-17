package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.BehaviorResult
import cc.mewcraft.wakame.item.behavior.ConsumeContext
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object DamageOnConsume : ItemBehavior {

    override fun handleConsume(context: ConsumeContext): BehaviorResult {
        val player = context.player
        val itemstack = context.itemstack
        val damageOnConsume = itemstack.getProp(ItemPropTypes.DAMAGE_ON_CONSUME) ?: return BehaviorResult.PASS
        context.modifyReplacement(itemstack.clone().damage(damageOnConsume, player))
        return BehaviorResult.PASS
    }
}