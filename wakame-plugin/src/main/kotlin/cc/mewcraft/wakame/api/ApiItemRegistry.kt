package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.api.item.NekoItem
import cc.mewcraft.wakame.api.item.NekoItemRegistry
import cc.mewcraft.wakame.core.Identifiers
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.item.nekoItem
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack

object ApiItemRegistry : NekoItemRegistry {
    override fun get(id: String): NekoItem {
        return get(Identifiers.of(id))
    }

    override fun get(id: Key): NekoItem {
        return getOrNull(id) ?: throw IllegalArgumentException("No item type found with id: $id")
    }

    override fun get(itemStack: ItemStack): NekoItem {
        return getOrNull(itemStack) ?: throw IllegalArgumentException("ItemStack is not of a Koish item type")
    }

    override fun getOrNull(id: String?): NekoItem? {
        if (id == null) return null
        return getOrNull(Identifiers.of(id))
    }

    override fun getOrNull(id: Key?): NekoItem? {
        if (id == null) return null
        return KoishRegistries.ITEM[id]?.let(::ApiItemWrapper)
    }

    override fun getOrNull(itemStack: ItemStack?): NekoItem? {
        return itemStack.nekoItem?.let(::ApiItemWrapper)
    }

    override fun getNonNamespaced(name: String): List<NekoItem> {
        return KoishRegistries.ITEM.getFuzzy(name).map(::ApiItemWrapper)
    }
}