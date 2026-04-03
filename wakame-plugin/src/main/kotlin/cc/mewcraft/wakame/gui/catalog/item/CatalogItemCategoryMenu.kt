package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window

/**
 * 类别菜单. 展示某类别的所有物品.
 */
internal class CatalogItemCategoryMenu(
    /** 该菜单展示的类别. */
    val category: CatalogItemCategory,
    /** 正在查看该菜单的玩家. */
    val viewer: Player,
) : CatalogItemMenu {

    private val settings: BasicMenuSettings = category.menuSettings

    private val primaryGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(*settings.structure)
        .addIngredient(
            '.', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("background").resolveToItemWrapper()
                }
        )
        .addIngredient(
            '<', BoundItem.pagedBuilder()
                .setItemProvider { _, gui ->
                    if (gui.page <= 0)
                        settings.getIcon("background").resolveToItemWrapper()
                    else
                        settings.getIcon("prev_page").resolveToItemWrapper {
                            standard {
                                component("current_page", Component.text(gui.page + 1))
                                component("total_page", Component.text(gui.pageCount))
                            }
                        }
                }
                .addClickHandler { _, gui, _ ->
                    gui.page -= 1
                }
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
                .addClickHandler { _, gui, _ ->
                    gui.page += 1
                }
        )
        .addIngredient(
            'b', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("back").resolveToItemWrapper()
                }
                .addClickHandler { _, click ->
                    CatalogItemMenuStacks.pop(click.player)
                }
                .build()
        )
        .addIngredient('x', category.contentMarker)
        .setContent(category.items.map { itemRef ->
            Item.builder()
                .setItemProvider { _ ->
                    ItemWrapper(itemRef.createItemStack())
                }
                .addClickHandler { _, click ->
                    val clickType = click.clickType
                    val player = click.player
                    val preferredState = when (clickType) {
                        ClickType.LEFT, ClickType.RIGHT -> LookupState.SOURCE
                        ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> LookupState.USAGE
                        else -> return@addClickHandler
                    }
                    // 兜底查询另一个方向: 允许 loot table / single source 类输入图标直接点开
                    val fallbackState = if (preferredState == LookupState.SOURCE) LookupState.USAGE else LookupState.SOURCE
                    val preferredGuis = CatalogItemNodeGuiManager.getGui(itemRef, preferredState)
                    val (state, guis) = if (preferredGuis.isNotEmpty()) {
                        preferredState to preferredGuis
                    } else {
                        fallbackState to CatalogItemNodeGuiManager.getGui(itemRef, fallbackState)
                    }
                    if (guis.isEmpty()) return@addClickHandler
                    CatalogItemMenuStacks.push(viewer, CatalogItemFocusMenu(itemRef, state, viewer, guis))
                }
                .build()
        })
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