package cc.mewcraft.wakame.item.behavior.impl

import cc.mewcraft.wakame.feature.ProxyServerSwitcher
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object Connect : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val player = context.player
        val connect = context.itemstack.getProp(ItemPropTypes.CONNECT) ?: return InteractionResult.PASS
        ProxyServerSwitcher.switch(player, connect)
        return InteractionResult.SUCCESS_AND_CANCEL
    }
}