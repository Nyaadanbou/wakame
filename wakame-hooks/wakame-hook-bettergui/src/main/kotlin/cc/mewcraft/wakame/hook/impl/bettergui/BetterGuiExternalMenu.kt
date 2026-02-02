package cc.mewcraft.wakame.hook.impl.bettergui

import cc.mewcraft.wakame.integration.externalmenu.ExternalMenu
import me.hsgamer.bettergui.BetterGUI
import me.hsgamer.bettergui.manager.MenuManager
import org.bukkit.entity.Player

object BetterGuiExternalMenu : ExternalMenu {

    override fun open(player: Player, menuId: String, menuArgs: Array<String>, bypass: Boolean) {
        val menuManager = BetterGUI.getInstance().get(MenuManager::class.java)
        menuManager.openMenu(menuId, player, menuArgs, true)
    }
}