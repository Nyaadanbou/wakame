package cc.mewcraft.wakame.core

import org.bukkit.inventory.ItemStack


object ItemXRegistry {
    private val builderMap: HashMap<String, ItemXFactory> = HashMap()
    private val builderList: ArrayList<ItemXFactory> = ArrayList()

    operator fun get(uid: String): ItemX? {
        val split = uid.split(":")
        if (split.size != 2) return null
        return get(split[0], split[1])
    }

    operator fun get(plugin: String, identifier: String): ItemX? {
        val builder = builderMap[plugin] ?: return null
        return builder.create(plugin, identifier)
    }

    operator fun get(itemStack: ItemStack): ItemX? {
        if (itemStack.isEmpty)
            return null
        for (builder in builderList) {
            val itemX = builder.create(itemStack) ?: continue
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
