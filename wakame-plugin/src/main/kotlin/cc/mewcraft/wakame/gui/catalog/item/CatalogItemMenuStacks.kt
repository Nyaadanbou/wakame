package cc.mewcraft.wakame.gui.catalog.item

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.WindowManager
import java.util.concurrent.TimeUnit

object CatalogItemMenuStacks {

    // 注意!
    // 由于设置了 expireAfterAccess, 即使玩家退出服务器, 其菜单栈依然会驻留在此一段时间
    // 也就是说菜单栈里面被 CatalogItemMenu 直接持有的 BukkitPlayer 实例也会驻留在此一段时间
    private val menuStackMap: LoadingCache<Player, ArrayDeque<CatalogItemMenu>> = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .weakKeys()
        .build(CacheLoader.from { _ -> ArrayDeque() })

    /**
     * 获取 [player] 的菜单栈的大小.
     */
    fun size(player: Player): Int {
        val stack = getStack(player)
        return stack.size
    }

    /**
     * 为 [player] 打开新的 [menu].
     *
     * 此函数会将新的菜单放在栈的顶部.
     */
    fun push(player: Player, menu: CatalogItemMenu) {
        val stack = getStack(player)
        stack.addLast(menu)
        stack.last().open()
    }

    /**
     * 返回 [player] 当前查看的菜单.
     *
     * 该函数不会移除位于栈顶部的菜单.
     */
    fun peek(player: Player): CatalogItemMenu? {
        val stack = getStack(player)
        return stack.lastOrNull()
    }

    /**
     * 使 [player] 返回上一级菜单.
     *
     * 该函数会移除位于栈顶部的菜单.
     */
    fun pop(player: Player) {
        val stack = getStack(player)

        // 如果栈已经空了, 则仅关闭当前菜单
        if (stack.isEmpty()) {
            WindowManager.getInstance().getOpenWindow(player)?.close()
            removeStack(player)
            return
        }

        // 如果栈只有当前菜单, 则关闭当前菜单并从栈移除
        if (stack.size == 1) {
            WindowManager.getInstance().getOpenWindow(player)?.close()
            removeStack(player)
            stack.removeLast()
            return
        }

        // 移除最顶层菜单
        stack.removeLast()
        // 打开最顶层菜单
        stack.last().open()
    }

    /**
     * 将 [player] 的菜单栈修改为 [menus], 并打开栈的顶部的菜单.
     *
     * 索引较小的菜单更靠近栈的底部, 反之索引较大的菜单更靠近栈的顶部.
     */
    fun rewrite(player: Player, vararg menus: CatalogItemMenu) {
        if (menus.isEmpty()) return
        val newStack = ArrayDeque(menus.toList())
        setStack(player, newStack)
        newStack.last().open()
    }

    //// 方便函数

    internal fun getStack(player: Player): ArrayDeque<CatalogItemMenu> {
        return menuStackMap[player]
    }

    internal fun setStack(player: Player, stack: ArrayDeque<CatalogItemMenu>) {
        menuStackMap.put(player, stack)
    }

    internal fun removeStack(player: Player) {
        menuStackMap.invalidate(player)
    }
}
