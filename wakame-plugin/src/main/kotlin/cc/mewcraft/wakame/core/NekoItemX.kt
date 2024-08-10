package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class NekoItemX(
    override val itemId: String,
) : ItemX {
    override val plugin: String = "wakame"
    override fun createItemStack(): ItemStack? {
        val key = Key.key(itemId.replaceFirst('/', ':'))
        val nekoItem = ItemRegistry.CUSTOM.find(key)
        val nekoStack = nekoItem?.realize()
        val itemStack = nekoStack?.itemStack
        return itemStack
    }

    override fun createItemStack(player: Player): ItemStack? {
        val key = Key.key(itemId.replaceFirst('/', ':'))
        val nekoItem = ItemRegistry.CUSTOM.find(key)
        val nekoStack = nekoItem?.realize(player.toUser())
        val itemStack = nekoStack?.itemStack
        return itemStack
    }

    override fun matches(itemStack: ItemStack): Boolean {
        val nekoStack = itemStack.tryNekoStack ?: return false
        val key = nekoStack.key()
        return "${key.namespace()}/${key.value()}" == itemId
    }

}

class NekoItemXBuilder : ItemXBuilder<NekoItemX> {
    override val available: Boolean = true
    override fun byItemStack(itemStack: ItemStack): NekoItemX? {
        val nekoStack = itemStack.tryNekoStack ?: return null
        val key = nekoStack.key()
        return NekoItemX("${key.namespace()}/${key.value()}")
    }

    override fun byReference(plugin: String, itemId: String): NekoItemX? {
        if (plugin != "wakame") return null
        return NekoItemX(itemId)
    }

    //TODO 注册

}