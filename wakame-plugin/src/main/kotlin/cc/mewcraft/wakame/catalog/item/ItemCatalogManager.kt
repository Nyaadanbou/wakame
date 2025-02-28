package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.gui.catalog.item.ItemCatalogMenu
import cc.mewcraft.wakame.gui.catalog.item.MainMenu
import org.bukkit.entity.Player
import java.util.*

object ItemCatalogManager {

    private val uuidToStack: MutableMap<UUID, ArrayDeque<ItemCatalogMenu>> = mutableMapOf()

    /**
     * 获取特定 [Player] 当前的菜单队列.
     */
    fun getMenuStack(player: Player): ArrayDeque<ItemCatalogMenu> {
        return uuidToStack[player.uniqueId] ?: ArrayDeque<ItemCatalogMenu>()
    }

    /**
     * 以传入的 [MainMenu] 为起点创建新的玩家菜单队列.
     */
    fun newMenuStack(player: Player, mainMenu: MainMenu): ArrayDeque<ItemCatalogMenu> {
        val stack = ArrayDeque<ItemCatalogMenu>(listOf(mainMenu))
        uuidToStack[player.uniqueId] = stack
        return stack
    }

}