package cc.mewcraft.wakame.gui.craftingstation

import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

/**
 * 合成站中用来预览配方的菜单. 不涉及物品和标题的动态变化.
 */
internal class CraftingPreviewMenu(
    /** 上级菜单. */
    val parent: CraftingStationMenu,
    /** 所预览的配方. */
    val recipe: Recipe,
    /** 正在查看该菜单的玩家. */
    val viewer: Player,
) {
    private val settings: BasicMenuSettings
        get() = parent.station.previewMenuSettings

    private val previewGui: PagedGui<Item> = PagedGui.itemsBuilder()
        .setStructure(*settings.structure)
        .addIngredient(
            '.', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("background").resolveToItemWrapper()
                }.build()
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
            'c', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("craft").resolveToItemWrapper()
                }
                .addClickHandler { _, click ->
                    val clickType = click.clickType
                    val player = click.player
                    when (clickType) {
                        // 单击合成
                        ClickType.LEFT, ClickType.RIGHT -> {
                            if (recipe.match(player).isAllowed)
                                tryCraft(recipe, player)
                            else
                                notifyFail(player)
                        }
                        // 潜行合成8次
                        ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                            var count = 0
                            if (recipe.match(player).isAllowed) {
                                do {
                                    tryCraft(recipe, player)
                                    count++
                                } while (
                                    recipe.match(player).isAllowed &&
                                    count < 8
                                )
                            } else notifyFail(player)
                        }
                        // 丢弃一键合成全部
                        ClickType.DROP, ClickType.CONTROL_DROP -> {
                            if (recipe.match(player).isAllowed) {
                                do {
                                    tryCraft(recipe, player)
                                } while (recipe.match(player).isAllowed)
                            } else notifyFail(player)
                        }

                        else -> {}
                    }
                }
        )
        .addIngredient(
            'b', Item.builder()
                .setItemProvider { _ ->
                    settings.getIcon("back").resolveToItemWrapper()
                }
                .addClickHandler { _, _ ->
                    val previousStationMenu = parent
                    previousStationMenu.stationSession.updateRecipeMatcherResults()
                    previousStationMenu.update()
                    previousStationMenu.open()
                }
        )
        .addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .addIngredient('o', Item.simple(recipe.output.displayItemStack(settings)))
        .setContent(recipe.input.map { input ->
            Item.simple(input.displayItemStack(settings))
        })
        .build()

    private val previewWindowBuilder: Window.Builder.Normal.Split = Window.builder().apply {
        setUpperGui(previewGui)
        setTitle(settings.title)
    }

    fun open() {
        previewWindowBuilder.open(viewer)
    }
}
