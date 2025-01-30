package cc.mewcraft.wakame.core

import org.bukkit.inventory.ItemStack


object ItemXFactoryRegistry {
    private val factoryMap: HashMap<String, ItemXFactory> = HashMap()
    private val factoryList: ArrayList<ItemXFactory> = ArrayList()

    operator fun get(uid: String): ItemX? {
        val split = uid.split(":")
        if (split.size != 2) return null
        return get(split[0], split[1])
    }

    operator fun get(plugin: String, identifier: String): ItemX? {
        val builder = factoryMap[plugin] ?: return null
        return builder.create(plugin, identifier)
    }

    operator fun get(itemStack: ItemStack): ItemX? {
        if (itemStack.isEmpty)
            return null
        for (builder in factoryList) {
            val itemX = builder.create(itemStack) ?: continue
            return itemX
        }
        return null
    }

    fun register(plugin: String, itemXFactory: ItemXFactory) {
        factoryMap[plugin.lowercase()] = itemXFactory
        factoryList += itemXFactory
    }

    fun unregister(pluginId: String) {
        factoryMap.remove(pluginId.lowercase())
        factoryList.removeIf { it.plugin.lowercase() == pluginId.lowercase() }
    }

    fun unregisterAll() {
        factoryMap.clear()
        factoryList.clear()
    }
}
