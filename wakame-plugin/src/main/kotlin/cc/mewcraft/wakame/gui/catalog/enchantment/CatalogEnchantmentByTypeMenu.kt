package cc.mewcraft.wakame.gui.catalog.enchantment

import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentInitializer
import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentMenuSettings
import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentTagIcons
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.resolveToItemWrapper
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

internal class CatalogEnchantmentByTypeMenu(
    val viewer: Player,
) : CatalogEnchantmentMenu {

    private val settings: BasicMenuSettings = CatalogEnchantmentMenuSettings.getMenuSettings("by_type")

    private val primaryGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(*settings.structure)
        .addCommonIngredients(settings)
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .setContent(
            CatalogEnchantmentInitializer.enchantableItemTags().map { tagEntry ->
                Item.builder()
                    .setItemProvider { _ ->
                        CatalogEnchantmentTagIcons.getIconOrDefault(tagEntry.tagValue).resolveToItemWrapper()
                    }
                    .addClickHandler { _, _ ->
                        CatalogEnchantmentMenuStacks.push(
                            viewer,
                            CatalogEnchantmentByTypeResultMenu(viewer, tagEntry)
                        )
                    }
                    .build()
            }
        )
        .build()

    private val primaryWindow: Window = Window.builder()
        .setUpperGui(primaryGui)
        .setViewer(viewer)
        .setTitle(settings.title)
        .build()

    override fun open() {
        primaryWindow.open()
    }

    override fun close() {
        primaryWindow.close()
    }
}
