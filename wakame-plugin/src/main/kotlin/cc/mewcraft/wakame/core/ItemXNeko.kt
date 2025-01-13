package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.nekoItem
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.item.template.ItemGenerationContexts
import cc.mewcraft.wakame.item.template.ItemGenerationTriggers
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

    override fun valid(): Boolean {
        return getArchetype() != null
    }

    override fun createItemStack(amount: Int, player: Player?): ItemStack? {
        val archetype = getArchetype() ?: return null
        if (player == null) {
            val context = ItemGenerationContexts.create(
                // 始终以等级 0 生成
                trigger = ItemGenerationTriggers.direct(0),
                // 设置为物品的 key
                target = archetype.id,
                // 随机种子始终为 0
                seed = 0
            )
            return archetype.realize(context).wrapped
        } else {
            return archetype.realize(player.toUser()).wrapped
        }
    }

    override fun matches(itemStack: ItemStack): Boolean {
        val nekoItemId = itemStack.nekoItem?.id ?: return false
        val transformed = "${nekoItemId.namespace()}/${nekoItemId.value()}"
        return transformed == identifier
    }

    override fun displayName(): String {
        return getArchetype()?.plainName ?: return DEFAULT_DISPLAY_NAME
    }

    private fun getArchetype(): NekoItem? {
        val transformed = identifier.replaceFirst('/', ':')
        val nekoItemId = Key.key(transformed)
        return KoishRegistries.ITEM[nekoItemId]
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
        return if (plugin == this.plugin)
            ItemXNeko(identifier)
        else null
    }
}