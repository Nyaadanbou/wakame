package cc.mewcraft.wakame.gui.catalog.enchantment

import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentInitializer
import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentMenuSettings
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

internal class CatalogEnchantmentAllMenu(
    val viewer: Player,
) : CatalogEnchantmentMenu {

    private val settings: BasicMenuSettings = CatalogEnchantmentMenuSettings.getMenuSettings("all")

    private val primaryGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(*settings.structure)
        .addIngredient(
            '.', Item.builder()
                .setItemProvider { _ -> settings.getIcon("background").resolveToItemWrapper() }
        )
        .addIngredient(
            '<', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page <= 0) settings.getIcon("background").resolveToItemWrapper()
                    else settings.getIcon("prev_page").resolveToItemWrapper {
                        standard {
                            component("current_page", Component.text(gui.page + 1))
                            component("total_page", Component.text(gui.pageCount))
                        }
                    }
                }
                .addClickHandler { _, gui, _ -> gui.page -= 1 }
        )
        .addIngredient(
            '>', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page >= gui.pageCount - 1) settings.getIcon("background").resolveToItemWrapper()
                    else settings.getIcon("next_page").resolveToItemWrapper {
                        standard {
                            component("current_page", Component.text(gui.page + 1))
                            component("total_page", Component.text(gui.pageCount))
                        }
                    }
                }
                .addClickHandler { _, gui, _ -> gui.page += 1 }
        )
        .addIngredient(
            'b', Item.builder()
                .setItemProvider { _ -> settings.getIcon("back").resolveToItemWrapper() }
                .addClickHandler { _, click -> CatalogEnchantmentMenuStacks.pop(click.player) }
        )
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .setContent(CatalogEnchantmentInitializer.allEntries().map { buildEnchantmentItem(it) })
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
