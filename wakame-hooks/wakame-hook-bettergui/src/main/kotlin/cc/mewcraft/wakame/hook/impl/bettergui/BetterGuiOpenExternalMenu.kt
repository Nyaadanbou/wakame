package cc.mewcraft.wakame.hook.impl.bettergui

import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.behavior.impl.SimpleInteract
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import me.hsgamer.bettergui.BetterGUI
import me.hsgamer.bettergui.manager.MenuManager

object BetterGuiOpenExternalMenu : SimpleInteract {

    private val betterGui: BetterGUI
        get() = BetterGUI.getInstance()

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val openExternalMenu = itemstack.getProp(ItemPropTypes.OPEN_EXTERNAL_MENU) ?: return InteractionResult.PASS
        val menuId = openExternalMenu.menuId
        val menuArgs = openExternalMenu.menuArgs.toTypedArray()
        betterGui.get(MenuManager::class.java).openMenu(menuId, player, menuArgs, true)
        return InteractionResult.SUCCESS
    }
}