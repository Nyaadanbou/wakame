package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.api.block.BlockManager
import cc.mewcraft.wakame.api.block.NekoBlockRegistry
import cc.mewcraft.wakame.api.item.NekoItemRegistry
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.tileentity.TileEntityManager
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.item.CustomNekoItemRealizer
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.isNeko
import cc.mewcraft.wakame.item.shadowNeko
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
internal class ApiNekooProvider : Nekoo, KoinComponent {
    override fun getTileEntityManager(): TileEntityManager? {
        TODO("Not yet implemented")
    }

    override fun getBlockManager(): BlockManager? {
        TODO("Not yet implemented")
    }

    override fun getBlockRegistry(): NekoBlockRegistry? {
        TODO("Not yet implemented")
    }

    override fun getItemRegistry(): NekoItemRegistry? {
        return ApiItemRegistry
    }

    override fun registerProtectionIntegration(integration: ProtectionIntegration) {
        ProtectionManager.integrations += integration
    }

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