package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.api.item.NekoItem
import cc.mewcraft.wakame.api.item.NekoItemRegistry
import cc.mewcraft.wakame.item.nekoItem
import cc.mewcraft.wakame.registry.ItemRegistry
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack

object ApiItemRegistry : NekoItemRegistry {
    override fun get(id: String): NekoItem {
        return get(Key.key(id))
    }

    override fun get(id: Key): NekoItem {
        return getOrNull(id) ?: throw IllegalArgumentException("No nekoo item with id $id found")
    }

    override fun get(itemStack: ItemStack): NekoItem {
        return getOrNull(itemStack) ?: throw IllegalArgumentException("ItemStack is not a nekoo item")
    }

    override fun getOrNull(id: String): NekoItem? {
        return getOrNull(Key.key(id))
    }

    override fun getOrNull(id: Key): NekoItem? {
        return ItemRegistry.CUSTOM.find(id)?.let(::ApiItemWrapper)
    }

    override fun getOrNull(itemStack: ItemStack): NekoItem? {
        return itemStack.nekoItem?.let(::ApiItemWrapper)
    }

    override fun getNonNamespaced(name: String): List<NekoItem> {
        return ItemRegistry.CUSTOM_FUZZY.getFuzzy(name).map(::ApiItemWrapper)
    }
}