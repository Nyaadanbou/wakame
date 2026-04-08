package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object Transfer : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val player = context.player
        val transfer = context.itemstack.getProp(ItemPropTypes.TRANSFER) ?: return InteractionResult.PASS
        player.transfer(transfer.host, transfer.port)
        return InteractionResult.SUCCESS_AND_CANCEL
    }
}