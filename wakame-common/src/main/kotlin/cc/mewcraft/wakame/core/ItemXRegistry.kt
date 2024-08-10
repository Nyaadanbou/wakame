package cc.mewcraft.wakame.core

import org.bukkit.inventory.ItemStack
import java.util.*


object ItemXRegistry {
    private val builders: MutableMap<String, ItemXBuilder<out ItemX>> = mutableMapOf()
    fun fromReference(plugin: String, itemId: String): ItemX? {
        val itemXBuilder = builders[plugin] ?: return null
        return itemXBuilder.byReference(plugin, itemId)
    }

    fun fromItemStack(itemStack: ItemStack): ItemX? {
        for (builder in builders) {
            val itemX = builder.value.byItemStack(itemStack) ?: continue
            return itemX
        }
        return null
    }

    fun registerBuilder(plugin: String, itemXBuilder: ItemXBuilder<out ItemX>) {
        builders[plugin.lowercase(Locale.getDefault())] = itemXBuilder
    }

    fun unregisterBuilder(pluginId: String) {
        builders.remove(pluginId.lowercase(Locale.getDefault()))
    }

    fun unregisterAll() {
        builders.clear()
    }
}

interface ItemXBuilder<T> {

    /**
     * 该物品库插件是否已经完成加载.
     */
    val available: Boolean

    /**
     * 通过 [ItemStack] 构建该物品库插件对应的 [ItemX].
     * 构建失败则返回空.
     */
    fun byItemStack(itemStack: ItemStack): T?

    /**
     * 通过 通用物品标识 构建该物品库插件对应的 [ItemX].
     * 构建失败则返回空.
     */
    fun byReference(plugin: String, itemId: String): T?

}