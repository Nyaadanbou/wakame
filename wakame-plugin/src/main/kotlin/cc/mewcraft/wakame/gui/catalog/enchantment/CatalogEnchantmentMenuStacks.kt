package cc.mewcraft.wakame.gui.catalog.enchantment

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.WindowManager
import java.util.concurrent.TimeUnit

object CatalogEnchantmentMenuStacks {

    private val menuStackMap: LoadingCache<Player, ArrayDeque<CatalogEnchantmentMenu>> = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .weakKeys()
        .build(CacheLoader.from { _ -> ArrayDeque() })

    fun size(player: Player): Int {
        return getStack(player).size
    }

    fun push(player: Player, menu: CatalogEnchantmentMenu) {
        val stack = getStack(player)
        stack.addLast(menu)
        stack.last().open()
    }

    fun peek(player: Player): CatalogEnchantmentMenu? {
        return getStack(player).lastOrNull()
    }

    fun pop(player: Player) {
        val stack = getStack(player)

        if (stack.isEmpty()) {
            WindowManager.getInstance().getOpenWindow(player)?.close()
            removeStack(player)
            return
        }

        if (stack.size == 1) {
            WindowManager.getInstance().getOpenWindow(player)?.close()
            removeStack(player)
            stack.removeLast()
            return
        }

        stack.removeLast()
        stack.last().open()
    }

    fun rewrite(player: Player, vararg menus: CatalogEnchantmentMenu) {
        if (menus.isEmpty()) return
        val newStack = ArrayDeque(menus.toList())
        setStack(player, newStack)
        newStack.last().open()
    }

    internal fun getStack(player: Player): ArrayDeque<CatalogEnchantmentMenu> {
        return menuStackMap[player]
    }

    internal fun setStack(player: Player, stack: ArrayDeque<CatalogEnchantmentMenu>) {
        menuStackMap.put(player, stack)
    }

    internal fun removeStack(player: Player) {
        menuStackMap.invalidate(player)
    }

    internal fun clearStacks() {
        menuStackMap.invalidateAll()
    }
}
