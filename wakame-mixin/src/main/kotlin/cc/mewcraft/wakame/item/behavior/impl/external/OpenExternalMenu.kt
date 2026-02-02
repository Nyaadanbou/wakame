package cc.mewcraft.wakame.item.behavior.impl.external

import cc.mewcraft.wakame.integration.externalmenu.ExternalMenu
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.behavior.impl.SimpleInteract
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes

object OpenExternalMenu : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val openExtMenu = itemstack.getProp(ItemPropTypes.OPEN_EXTERNAL_MENU) ?: return InteractionResult.PASS
        val menuId = openExtMenu.menuId
        val menuArgs = openExtMenu.menuArgs.toTypedArray()
        ExternalMenu.open(player, menuId, menuArgs, true)
        return InteractionResult.SUCCESS
    }
}