package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.CatalogItemMenuSettings
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.changeTitle
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 聚焦于一个物品, 展示其来源/用途的菜单.
 */
internal class CatalogItemFocusMenu(
    /**
     * 该菜单聚焦的物品.
     */
    val item: ItemRef,
    /**
     * 该菜单的检索状态.
     */
    val state: LookupState,
    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
    /**
     * 该菜单中的各页配方 [Gui].
     */
    val guis: List<CatalogRecipeGui>,
) : CatalogItemMenu {

    /**
     * 菜单的 [BasicMenuSettings].
     */
    private val settings: BasicMenuSettings = CatalogItemMenuSettings.getMenuSettings("paged_catalog_recipes")

    /**
     * 菜单的 [PagedGui].
     *
     * - `x`: catalog_recipe_gui
     * - `.`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `b`: back
     */
    private val primaryGui: PagedGui<Gui> = PagedGui.guis { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('b', BackItem())
        builder.setContent(guis.map { it.gui })
    }

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window = Window.single().apply {
        setGui(primaryGui)
        setViewer(viewer)
        setTitle(guis.first().title)
    }.build()

    override fun open() {
        primaryWindow.open()
    }

    override fun close() {
        primaryWindow.close()
    }

    /**
     * **背景** 占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

        }
    }

    /**
     * **上一页** 的图标.
     */
    inner class PrevItem : PageItem(false) {

        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasPreviousPage()) {
                return settings.getSlotDisplay("background").resolveToItemWrapper()
            }
            return settings.getSlotDisplay("prev_page").resolveToItemWrapper {
                standard {
                    component("current_page", Component.text(primaryGui.currentPage + 1))
                    component("total_page", Component.text(primaryGui.pageAmount))
                }
            }
        }

        // 刷新菜单标题
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            super.handleClick(clickType, player, event)
            primaryWindow.changeTitle(guis[primaryGui.currentPage].title)
        }
    }

    /**
     * **下一页** 的图标.
     */
    inner class NextItem : PageItem(true) {

        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasNextPage()) {
                return settings.getSlotDisplay("background").resolveToItemWrapper()
            }
            return settings.getSlotDisplay("next_page").resolveToItemWrapper {
                standard {
                    component("current_page", Component.text(primaryGui.currentPage + 1))
                    component("total_page", Component.text(primaryGui.pageAmount))
                }
            }
        }

        // 刷新菜单标题
        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            super.handleClick(clickType, player, event)
            primaryWindow.changeTitle(guis[primaryGui.currentPage].title)
        }
    }

    /**
     * **返回** 的图标.
     */
    inner class BackItem : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("back").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            CatalogItemMenuStacks.pop(player)
        }
    }
}