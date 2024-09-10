package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.template.ItemGenerationContexts
import cc.mewcraft.wakame.item.template.ItemGenerationTriggers
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ItemXNeko(
    identifier: String,
) : ItemXAbstract(ItemXFactoryNeko.plugin, identifier) {
    companion object {
        const val DEFAULT_RENDER_NAME = "<white>UNKNOWN</white>"
    }

    override fun isValid(): Boolean {
        val nekoItemId = Key.key(identifier.replaceFirst('/', ':'))
        return ItemRegistry.CUSTOM.find(nekoItemId) != null
    }

    override fun createItemStack(): ItemStack? {
        val nekoItemId = Key.key(identifier.replaceFirst('/', ':'))
        val nekoItem = ItemRegistry.CUSTOM.find(nekoItemId)
        val context = ItemGenerationContexts.create(
            // 始终以等级 0 生成
            trigger = ItemGenerationTriggers.direct(0),
            // 设置为物品的 key
            target = nekoItemId,
            // 随机种子始终为 0
            seed = 0
        )
        val nekoStack = nekoItem?.realize(context)
        val itemStack = nekoStack?.itemStack
        return itemStack
    }

    override fun createItemStack(player: Player): ItemStack? {
        val nekoItemId = Key.key(identifier.replaceFirst('/', ':'))
        val nekoItem = ItemRegistry.CUSTOM.find(nekoItemId)
        val nekoStack = nekoItem?.realize(player.toUser())
        val itemStack = nekoStack?.itemStack
        return itemStack
    }

    override fun matches(itemStack: ItemStack): Boolean {
        val nekoStack = itemStack.tryNekoStack ?: return false
        val nekoStackId = nekoStack.id
        return "${nekoStackId.namespace()}/${nekoStackId.value()}" == identifier
    }

    override fun renderName(): String {
        val nekoItemId = Key.key(identifier.replaceFirst('/', ':'))
        val nekoItem = ItemRegistry.CUSTOM.find(nekoItemId) ?: return DEFAULT_RENDER_NAME
        return nekoItemId.asString() // TODO
    }
}

object ItemXFactoryNeko : ItemXFactory {
    override val plugin: String = "wakame"

    override val isValid: Boolean = true

    override fun byItemStack(itemStack: ItemStack): ItemXNeko? {
        val nekoStack = itemStack.tryNekoStack ?: return null
        val nekoStackId = nekoStack.id
        return ItemXNeko("${nekoStackId.namespace()}/${nekoStackId.value()}")
    }

    override fun byUid(plugin: String, identifier: String): ItemXNeko? {
        if (plugin != this.plugin)
            return null
        return ItemXNeko(identifier)
    }
}