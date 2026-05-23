package cc.mewcraft.wakame.gui.catalog.enchantment

import cc.mewcraft.wakame.catalog.enchantment.CatalogEnchantmentMenuSettings
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.resolveToItemWrapper
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

internal class CatalogEnchantmentMainMenu(
    val viewer: Player,
) : CatalogEnchantmentMenu {

    private val settings: BasicMenuSettings = CatalogEnchantmentMenuSettings.getMenuSettings("main")

    private val primaryGui: Gui = Gui.builder()
        .setStructure(*settings.structure)
        .addIngredient(
            '.', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("background").resolveToItemWrapper()
                }
        )
        .addIngredient(
            'a', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("view_all").resolveToItemWrapper()
                }
                .addClickHandler { _, _ ->
                    CatalogEnchantmentMenuStacks.push(viewer, CatalogEnchantmentAllMenu(viewer))
                }
        )
        .addIngredient(
            'b', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("by_item").resolveToItemWrapper()
                }
                .addClickHandler { _, _ ->
                    CatalogEnchantmentMenuStacks.push(viewer, CatalogEnchantmentByItemMenu(viewer))
                }
        )
        .addIngredient(
            'c', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("by_type").resolveToItemWrapper()
                }
                .addClickHandler { _, _ ->
                    CatalogEnchantmentMenuStacks.push(viewer, CatalogEnchantmentByTypeMenu(viewer))
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
