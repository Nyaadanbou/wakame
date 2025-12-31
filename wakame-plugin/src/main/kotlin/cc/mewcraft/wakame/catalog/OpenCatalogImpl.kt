package cc.mewcraft.wakame.catalog

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemCategoryMenu
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMainMenu
import cc.mewcraft.wakame.gui.catalog.item.CatalogItemMenuStacks
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.behavior.impl.SimpleInteract
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.OpenCatalog
import cc.mewcraft.wakame.registry.DynamicRegistries
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object OpenCatalogImpl : SimpleInteract {

    override fun handleSimpleUse(context: UseContext): InteractionResult {
        val player = context.player
        val itemstack = context.itemstack
        val openCatalog = itemstack.getProp(ItemPropTypes.OPEN_CATALOG) ?: return InteractionResult.PASS

        val type = openCatalog.catalogType
        when (type) {
            "item" -> return handleOpenItemCatalog(player, itemstack, openCatalog)
        }

        return InteractionResult.PASS
    }

    private fun handleOpenItemCatalog(player: Player, item: ItemStack, openCatalog: OpenCatalog): InteractionResult {
        val catalogType = openCatalog.catalogType
        val catalogId = openCatalog.catalogId
        if (catalogId == null) {
            // 未指定类别, 则优先打开最近一次看过的菜单
            val last = CatalogItemMenuStacks.peek(player)
            if (last != null) {
                last.open()
                return InteractionResult.PASS
            }
            val menu = CatalogItemMainMenu(player)
            CatalogItemMenuStacks.rewrite(player, menu)
        } else {
            val category = DynamicRegistries.ITEM_CATEGORY[catalogId] ?: run {
                LOGGER.error("Found an unknown catalog item category id '$catalogId' for type '$catalogType' when opening catalog from item")
                return InteractionResult.PASS
            }
            val parent = CatalogItemMainMenu(player)
            val child = CatalogItemCategoryMenu(category, player)
            CatalogItemMenuStacks.rewrite(player, parent, child)
        }
        return InteractionResult.PASS
    }
}