package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.Nekoo
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.user.toUser
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent

/**
 * 使用插件实例来实现 Nekoo API.
 */
internal class NekooApiProvider : Nekoo, KoinComponent {
    override fun createItemStack(
        id: Key, source: Player?,
    ): ItemStack {
        val namespace = id.namespace()
        val path = id.value()
        return createItemStack(namespace, path, source)
    }

    override fun createItemStack(
        namespace: String, path: String, source: Player?,
    ): ItemStack {
        val nekoItem = ItemRegistry.CUSTOM.find(Key.key(namespace, path))
        if (nekoItem == null) {
            return ERROR_ITEM_STACK.clone()
        }

        val nekoStack: NekoStack
        if (source == null) {
            nekoStack = CustomNekoItemRealizer.realize(nekoItem)
        } else {
            val user = source.toUser()
            nekoStack = CustomNekoItemRealizer.realize(nekoItem, user)
        }

        return nekoStack.wrapped
    }

    override fun isNekoStack(itemStack: ItemStack): Boolean {
        return itemStack.isNeko
    }

    override fun getNekoItemId(itemStack: ItemStack): Key? {
        return itemStack.shadowNeko()?.id
    }

    companion object {
        private val ERROR_ITEM_STACK: ItemStack = ItemStack.of(Material.STONE).apply {
            editMeta { im ->
                im.itemName(text {
                    content("ERROR")
                    color(NamedTextColor.RED)
                })
            }
        }
    }
}