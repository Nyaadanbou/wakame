package cc.mewcraft.wakame.gui.craftingstation

import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item2.display.resolveToItemWrapper
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 合成站中用来预览配方的菜单.
 *
 * 这个菜单是"静态"的, 即不涉及物品和标题的变化.
 */
internal class CraftingPreviewMenu(
    /**
     * 该菜单的上级菜单.
     */
    val parent: CraftingStationMenu,
    /**
     * 该菜单所预览的配方.
     */
    val recipe: Recipe,
    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) {
    /**
     * 该菜单的基本设置.
     */
    private val settings: BasicMenuSettings
        get() = parent.station.previewMenuSettings

    /**
     * 合成站配方预览菜单的 [Gui].
     *
     * - '.': background
     * - 'i': input/choices
     * - 'o': output/result
     * - '<': prev_page
     * - '>': next_page
     * - 'c': craft
     * - 'b': back
     */
    private val previewGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('c', CraftItem(recipe))
        builder.addIngredient('b', BackItem())
        builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('o', SimpleItem(recipe.output.displayItemStack(settings)))
        builder.setContent(recipe.input.map { input -> SimpleItem(input.displayItemStack(settings)) })
    }

    /**
     * 合成站配方预览的 [Window].
     */
    private val previewWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(previewGui)
        setTitle(settings.title)
    }

    /**
     * 向指定玩家打开该合成站配方预览菜单.
     */
    fun open() {
        previewWindow.open(viewer)
    }

    /**
     * **背景占位** 的图标.
     */
    inner class BackgroundItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // do nothing
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
            return settings.getSlotDisplay("prev_page").resolveToItemWrapper()
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
            return settings.getSlotDisplay("next_page").resolveToItemWrapper()
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
            val previousStationMenu = parent
            previousStationMenu.stationSession.updateRecipeMatcherResults()
            previousStationMenu.update()
            previousStationMenu.open()
        }
    }

    /**
     * 触发点击合成的图标 [Item].
     */
    inner class CraftItem(
        private val recipe: Recipe,
    ) : AbstractCraftItem() {
        override fun getItemProvider(): ItemProvider {
            return settings.getSlotDisplay("craft").resolveToItemWrapper()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                // 单击合成
                ClickType.LEFT, ClickType.RIGHT -> {
                    if (recipe.match(player).isAllowed) {
                        tryCraft(recipe, player)
                    } else {
                        notifyFail(player)
                    }
                }

                // 潜行合成8次
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                    var count = 0
                    if (recipe.match(player).isAllowed) {
                        do {
                            tryCraft(recipe, player)
                            count++
                        } while (recipe.match(player).isAllowed && count < 8)
                    } else {
                        notifyFail(player)
                    }
                }

                // 丢弃一键合成全部
                ClickType.DROP, ClickType.CONTROL_DROP -> {
                    if (recipe.match(player).isAllowed) {
                        do {
                            tryCraft(recipe, player)
                        } while (recipe.match(player).isAllowed)
                    } else {
                        notifyFail(player)
                    }
                }

                else -> {
                    // do nothing
                }
            }
        }
    }
}
