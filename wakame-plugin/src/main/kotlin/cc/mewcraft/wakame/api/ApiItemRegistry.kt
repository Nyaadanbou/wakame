package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.api.item.KoishItem
import cc.mewcraft.wakame.api.item.KoishItemRegistry
import cc.mewcraft.wakame.item.nekoItem
import cc.mewcraft.wakame.registry2.DynamicRegistries
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack

object ApiItemRegistry : KoishItemRegistry {
    override fun get(id: String): KoishItem {
        return get(Identifiers.of(id))
    }

    override fun get(id: Key): KoishItem {
        return getOrNull(id) ?: throw IllegalArgumentException("No item type found with id: $id")
    }

    override fun get(itemStack: ItemStack): KoishItem {
        return getOrNull(itemStack) ?: throw IllegalArgumentException("ItemStack is not of a Koish item type")
    }

    override fun getOrNull(id: String?): KoishItem? {
        if (id == null) return null
        return getOrNull(Identifiers.of(id))
    }

    override fun getOrNull(id: Key?): KoishItem? {
        if (id == null) return null
        return DynamicRegistries.ITEM[id]?.let(::ApiItemWrapper)
    }

    override fun getOrNull(itemStack: ItemStack?): KoishItem? {
        return itemStack.nekoItem?.let(::ApiItemWrapper)
    }

    override fun getNonNamespaced(name: String): List<KoishItem> {
        return DynamicRegistries.ITEM.getFuzzy(name).map(::ApiItemWrapper)
    }
}