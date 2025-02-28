package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.gui.catalog.item.menu.ItemCatalogMenu
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.WindowManager
import java.util.*
import kotlin.collections.ArrayDeque

object ItemCatalogMenuStack {

    private val menuStackMap: HashMap<UUID, ArrayDeque<ItemCatalogMenu>> = HashMap()

    /**
     * 获取 [player] 的菜单队列.
     *
     * 如果玩家没有栈, 会先创建一个空栈然后返回.
     */
    fun get(player: Player): List<ItemCatalogMenu> {
        return menuStackMap.getOrPut(player.uniqueId, ::ArrayDeque)
    }

    /**
     * 为 [player] 打开新的 [menu].
     *
     * 此函数会将菜单放置在栈的顶部.
     */
    fun push(player: Player, menu: ItemCatalogMenu) {
        menuStackMap.getOrPut(player.uniqueId, ::ArrayDeque).addLast(menu)
        menu.open()
    }

    /**
     * 使 [player] 返回上一级菜单.
     *
     * 此函数会移除位于栈顶部的菜单.
     */
    fun pop(player: Player) {
        val stack: ArrayDeque<ItemCatalogMenu> = menuStackMap.getOrPut(player.uniqueId, ::ArrayDeque)

        // 如果栈已经空了, 则仅关闭当前菜单
        if (stack.isEmpty()) {
            WindowManager.getInstance().getOpenWindow(player)?.close()
            return
        }

        // 如果栈只有当前菜单, 则关闭当前菜单并从栈移除
        if (stack.size == 1) {
            stack.removeLast()
            WindowManager.getInstance().getOpenWindow(player)?.close()
            return
        }

        // 正常返回上一级菜单
        stack.removeLast()
        stack.last().open()
    }

    /**
     * 将 [player] 的菜单栈修改为 [menus], 并打开栈的顶部的菜单.
     *
     * 索引较小的菜单更靠近栈的底部, 反之索引较大的菜单更靠近栈的顶部.
     */
    fun rewrite(player: Player, vararg menus: ItemCatalogMenu) {
        if (menus.isEmpty()) return
        menuStackMap[player.uniqueId] = ArrayDeque(menus.toList())
        menus.last().open()
    }
}
