package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.catalog.item.CatalogItemMenuSettings
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.integration.permission.PermissionManager
import cc.mewcraft.wakame.item.SlotDisplay
import cc.mewcraft.wakame.item.resolveToItemWrapper
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.util.KoishKey
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

/**
 * 物品图鉴主菜单. 展示所有的物品类别.
 */
class CatalogItemMainMenu(
    /** 正在查看该菜单的玩家. */
    val viewer: Player,
) : CatalogItemMenu {

    private val categoryCache: HashMap<KoishKey, Item> = HashMap()
    private val settings: BasicMenuSettings = CatalogItemMenuSettings.getMenuSettings("main")

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
                    if (gui.page >= gui.pageCount - 1)
                        settings.getIcon("background").resolveToItemWrapper()
                    else
                        settings.getIcon("next_page").resolveToItemWrapper {
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
            's', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("search").resolveToItemWrapper()
                }
                .addClickHandler { _, _ ->
                    // TODO 物品搜索功能
                }
        )
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .setContent(
            DynamicRegistries.CATALOG_ITEM_CATEGORY
                .filter { category ->
                    val permission = category.permission ?: return@filter true
                    val world = viewer.world
                    val player = viewer.uniqueId
                    PermissionManager.hasPermission(world, player, permission).get()
                }
                .map { category ->
                    categoryCache.getOrPut(category.id) {
                        Item.builder()
                            .setItemProvider { _ ->
                                SlotDisplay.get(category.icon).resolveToItemWrapper()
                            }
                            .addClickHandler { _, _ ->
                                CatalogItemMenuStacks.push(viewer, CatalogItemCategoryMenu(category, viewer))
                            }
                            .build()
                    }
                }
                .toList()
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