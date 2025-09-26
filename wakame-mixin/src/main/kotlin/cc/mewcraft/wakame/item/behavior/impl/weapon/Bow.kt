package cc.mewcraft.wakame.item.behavior.impl.weapon

import cc.mewcraft.wakame.item.behavior.InteractionHand
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext

object Bow : Weapon {
    override fun handleSimpleUse(context: UseContext): InteractionResult {
        if (context.hand == InteractionHand.OFF_HAND) return InteractionResult.FAIL_AND_CANCEL
        return InteractionResult.SUCCESS
    }
}