package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.CatalogItemMenuSettings
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

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
     * 该菜单中的各页节点 [Gui].
     */
    val guis: List<CatalogItemNodeGui>,
) : CatalogItemMenu {

    /**
     * 菜单的 [BasicMenuSettings].
     */
    private val settings: BasicMenuSettings = CatalogItemMenuSettings.getMenuSettings("node")

    /**
     * 菜单的 [PagedGui].
     *
     * - `x`: node_gui
     * - `.`: background
     * - `<`: prev_page
     * - `>`: next_page
     * - `b`: back
     */
    private val primaryGui: PagedGui<Gui> = PagedGui.guisBuilder()
        .setStructure(*settings.structure)
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
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
                    primaryWindow.setTitle(guis[gui.page].title) // 刷新菜单标题
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
                    primaryWindow.setTitle(guis[gui.page].title) // 刷新菜单标题
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
        )
        .setContent(guis.map { it.gui })
        .build()

    /**
     * 菜单的 [Window].
     */
    private val primaryWindow: Window = Window.builder()
        .setUpperGui(primaryGui)
        .setViewer(viewer)
        .setTitle(guis.first().title)
        .build()

    override fun open() {
        primaryWindow.open()
    }

    override fun close() {
        primaryWindow.close()
    }
}