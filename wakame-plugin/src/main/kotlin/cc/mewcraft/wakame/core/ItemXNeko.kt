package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.nekoItem
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.item.template.ItemGenerationContexts
import cc.mewcraft.wakame.item.template.ItemGenerationTriggers
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXNeko(
    identifier: String,
) : ItemXAbstract(ItemXFactoryNeko.plugin, identifier) {
    companion object {
        const val DEFAULT_DISPLAY_NAME = "<white>未知物品</white>"
    }

    private fun getItem(): NekoItem? {
        val nekoItemId = Key.key(identifier.replaceFirst('/', ':'))
        return ItemRegistry.CUSTOM.getOrNull(nekoItemId)
    }

    override fun valid(): Boolean {
        return getItem() != null
    }

    override fun createItemStack(): ItemStack? {
        val nekoItem = getItem() ?: return null
        val context = ItemGenerationContexts.create(
            // 始终以等级 0 生成
            trigger = ItemGenerationTriggers.direct(0),
            // 设置为物品的 key
            target = nekoItem.id,
            // 随机种子始终为 0
            seed = 0
        )
        val nekoStack = nekoItem.realize(context)
        val itemStack = nekoStack.wrapped
        return itemStack
    }

    override fun createItemStack(player: Player): ItemStack? {
        return getItem()?.realize(player.toUser())?.wrapped
    }

    override fun matches(itemStack: ItemStack): Boolean {
        val nekoItemId = itemStack.nekoItem?.id ?: return false
        val transformed = "${nekoItemId.namespace()}/${nekoItemId.value()}"
        return transformed == identifier
    }

    override fun displayName(): String {
        return getItem()?.plainName ?: return DEFAULT_DISPLAY_NAME
    }
}

object ItemXFactoryNeko : ItemXFactory {
    override val plugin: String = "wakame"
    override val loaded: Boolean = true

    override fun create(itemStack: ItemStack): ItemXNeko? {
        val nekoStack = itemStack.shadowNeko(true) ?: return null
        val nekoStackId = nekoStack.id
        val transformed = "${nekoStackId.namespace()}/${nekoStackId.value()}"
        return ItemXNeko(transformed)
    }

    override fun create(plugin: String, identifier: String): ItemXNeko? {
        if (plugin != this.plugin)
            return null
        return ItemXNeko(identifier)
    }
}