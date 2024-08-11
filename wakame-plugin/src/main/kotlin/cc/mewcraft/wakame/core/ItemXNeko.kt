package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXNeko(
    identifier: String,
) : ItemXAbstract(ItemXFactoryNeko.plugin, identifier) {

    override fun createItemStack(): ItemStack? {
        val key = Key.key(identifier.replaceFirst('/', ':'))
        val nekoItem = ItemRegistry.CUSTOM.find(key)
        val nekoStack = nekoItem?.realize()
        val itemStack = nekoStack?.itemStack
        return itemStack
    }

    override fun createItemStack(player: Player): ItemStack? {
        val key = Key.key(identifier.replaceFirst('/', ':'))
        val nekoItem = ItemRegistry.CUSTOM.find(key)
        val nekoStack = nekoItem?.realize(player.toUser())
        val itemStack = nekoStack?.itemStack
        return itemStack
    }

    override fun matches(itemStack: ItemStack): Boolean {
        val nekoStack = itemStack.tryNekoStack ?: return false
        val key = nekoStack.key
        return "${key.namespace()}/${key.value()}" == identifier
    }
}

object ItemXFactoryNeko : ItemXFactory {
    override val plugin: String = "wakame"

    override val isValid: Boolean = true

    override fun byItemStack(itemStack: ItemStack): ItemXNeko? {
        val nekoStack = itemStack.tryNekoStack ?: return null
        val key = nekoStack.key()
        return ItemXNeko("${key.namespace()}/${key.value()}")
    }

    override fun byUid(plugin: String, itemId: String): ItemXNeko? {
        if (plugin != this.plugin)
            return null
        return ItemXNeko(itemId)
    }
}