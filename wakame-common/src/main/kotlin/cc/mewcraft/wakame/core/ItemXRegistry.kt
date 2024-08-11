package cc.mewcraft.wakame.core

import org.bukkit.inventory.ItemStack
import java.util.*


object ItemXRegistry {
    private val builderMap: HashMap<String, ItemXFactory> = HashMap()
    private val builderList: ArrayList<ItemXFactory> = ArrayList()

    fun byUid(uid: String): ItemX? {
        val split = uid.split(":")
        if (split.size != 2) return null
        return byUid(split[0], split[1])
    }

    fun byUid(plugin: String, identifier: String): ItemX? {
        val builder = builderMap[plugin] ?: return null
        return builder.byUid(plugin, identifier)
    }

    fun byItem(itemStack: ItemStack): ItemX? {
        for (builder in builderList) {
            val itemX = builder.byItemStack(itemStack) ?: continue
            return itemX
        }
        return null
    }

    fun register(plugin: String, itemXFactory: ItemXFactory) {
        builderMap[plugin.lowercase()] = itemXFactory
        builderList += itemXFactory
    }

    fun unregister(pluginId: String) {
        builderMap.remove(pluginId.lowercase())
        builderList.removeIf { it.plugin.lowercase() == pluginId.lowercase() }
    }

    fun unregisterAll() {
        builderMap.clear()
        builderList.clear()
    }
}
