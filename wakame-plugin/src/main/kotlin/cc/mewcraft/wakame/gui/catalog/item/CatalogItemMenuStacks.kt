package cc.mewcraft.wakame.gui.catalog.item

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.WindowManager
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayDeque
import kotlin.collections.last

object CatalogItemMenuStacks {

    // 注意!
    // 由于设置了 expireAfterAccess, 即使玩家退出服务器, 其菜单栈依然会驻留在此一段时间
    // 也就是说菜单栈里面被 CatalogItemMenu 直接持有的 BukkitPlayer 实例也会驻留在此一段时间
    private val menuStackMap: LoadingCache<UUID, ArrayDeque<CatalogItemMenu>> = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build(CacheLoader.from { _ -> ArrayDeque() })

    /**
     * 获取 [player] 的菜单栈的大小.
     */
    fun size(player: Player): Int {
        return menuStackMap[player.uniqueId]?.size ?: 0
    }

    /**
     * 为 [player] 打开新的 [menu].
     *
     * 此函数会将新的菜单放在栈的顶部.
     */
    fun push(player: Player, menu: CatalogItemMenu) {
        menuStackMap.get(player.uniqueId).addLast(menu)
        menu.open()
    }

    /**
     * 返回 [player] 当前查看的菜单.
     *
     * 该函数不会移除位于栈顶部的菜单.
     */
    fun peek(player: Player): CatalogItemMenu? {
        return menuStackMap.get(player.uniqueId)?.last()
    }

    /**
     * 使 [player] 返回上一级菜单.
     *
     * 该函数会移除位于栈顶部的菜单.
     */
    fun pop(player: Player) {
        val stack: ArrayDeque<CatalogItemMenu> = menuStackMap.get(player.uniqueId)

        // 如果栈已经空了, 则仅关闭当前菜单
        if (stack.isEmpty()) {
            WindowManager.getInstance().getOpenWindow(player)?.close()
            menuStackMap.invalidate(player.uniqueId)
            return
        }

        // 如果栈只有当前菜单, 则关闭当前菜单并从栈移除
        if (stack.size == 1) {
            stack.removeLast()
            WindowManager.getInstance().getOpenWindow(player)?.close()
            menuStackMap.invalidate(player.uniqueId)
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
    fun rewrite(player: Player, vararg menus: CatalogItemMenu) {
        if (menus.isEmpty()) return
        menuStackMap.put(player.uniqueId, ArrayDeque(menus.toList()))
        menus.last().open()
    }
}
