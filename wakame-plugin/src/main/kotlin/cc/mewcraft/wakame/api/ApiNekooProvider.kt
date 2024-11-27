package cc.mewcraft.wakame.api

import cc.mewcraft.wakame.api.block.BlockManager
import cc.mewcraft.wakame.api.block.NekoBlockRegistry
import cc.mewcraft.wakame.api.item.NekoItemRegistry
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.tileentity.TileEntityManager
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
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