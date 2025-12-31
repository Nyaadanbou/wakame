package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 类别菜单.
 * 展示某类别的所有物品.
 */
internal class CatalogItemCategoryMenu(
    /**
     * 该菜单展示的 [CatalogItemCategory].
     */
    val category: CatalogItemCategory,
    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) : CatalogItemMenu {

    /**
     * 菜单的 [BasicMenuSettings].
     */
    private val settings: BasicMenuSettings = category.menuSettings

    /**
     * 菜单的 [PagedGui].
     *
     * - `.`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `b`: back
     * - `x`: display
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('b', BackItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        // TODO 可以缓存, 只有重载时会变化
        // 类别菜单所展示的物品
        builder.setContent(category.items.map { itemRef -> DisplayItem(itemRef) })
    }

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window = Window.single().apply {
        setGui(primaryGui)
        setViewer(viewer)
        setTitle(settings.title)
    }.build()

    override fun open() {
        primaryWindow.open()
    }

    override fun close() {
        primaryWindow.close()
    }

    /**
     * 背景占位的图标.
     */
    inner class BackgroundItem : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

        }
    }

    /**
     * 上一页的图标.
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
    }

    /**
     * 下一页的图标.
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

    /**
     * **展示物品** 的图标.
     */
    inner class DisplayItem(
        val item: ItemRef,
    ) : AbstractItem() {

        override fun getItemProvider(): ItemProvider {
            return ItemWrapper(item.createItemStack())
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // 类别菜单无需判定是否套娃, 因为肯定是第一次对配方进行索引
            val state = when (clickType) {
                ClickType.LEFT, ClickType.RIGHT -> LookupState.SOURCE
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> LookupState.USAGE
                else -> return
            }
            // 要打开的菜单Gui列表为空，则不打开
            val guis = CatalogRecipeGuiManager.getGui(item, state)
            if (guis.isEmpty()) return

            CatalogItemMenuStacks.push(viewer, CatalogItemFocusMenu(item, state, viewer, guis))
        }
    }
}