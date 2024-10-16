package cc.mewcraft.wakame.gui.station

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.CraftingStationContext
import cc.mewcraft.wakame.display2.implementation.CraftingStationContext.*
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.gui.toItemProvider
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.station.recipe.StationRecipe
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.koin.core.component.KoinComponent
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
internal class PreviewMenu(
    /**
     * 该菜单所预览的配方.
     */
    val recipe: StationRecipe,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,

    /**
     * 该菜单的上级菜单.
     */
    val parent: StationMenu,
) : KoinComponent {
    /**
     * 该菜单的布局
     */
    private val layout: MenuLayout
        get() = parent.station.previewLayout

    /**
     * 合成站配方预览菜单的 [Gui].
     *
     * - 'X': background
     * - 'I': input/choices
     * - 'O': output/result
     * - '<': prev_page
     * - '>': next_page
     * - 'C': craft
     * - 'B': back
     */
    private val previewGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*layout.structure)
        builder.addIngredient('X', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('C', CraftItem(recipe))
        builder.addIngredient('B', BackItem())
        builder.addIngredient('I', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('O', SimpleItem(recipe.output.displayItemStack()))
        builder.setContent(recipe.input.map { SimpleItem(it.displayItemStack()) })
    }

    /**
     * 合成站配方预览的 [Window].
     */
    private val previewWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(previewGui)
        setTitle(layout.title)
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
            return layout.getIcon("background").render0().toItemProvider()
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
                return layout.getIcon("background").render0().toItemProvider()
            }
            return layout.getIcon("prev_page").render0().toItemProvider()
        }
    }

    /**
     * **下一页** 的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            if (!getGui().hasPreviousPage()) {
                return layout.getIcon("background").render0().toItemProvider()
            }
            return layout.getIcon("next_page").render0().toItemProvider()
        }
    }

    /**
     * **返回** 的图标.
     */
    inner class BackItem : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return layout.getIcon("back").render0().toItemProvider()
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
        private val recipe: StationRecipe,
    ) : AbstractCraftItem() {
        override fun getItemProvider(): ItemProvider {
            return layout.getIcon("craft").render0().toItemProvider()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                // 单击合成
                ClickType.LEFT, ClickType.RIGHT -> {
                    if (recipe.match(player).canCraft) {
                        tryCraft(recipe, player)
                    } else {
                        notifyFail(player)
                    }
                }

                // 潜行合成8次
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                    var count = 0
                    if (recipe.match(player).canCraft) {
                        do {
                            tryCraft(recipe, player)
                            count++
                        } while (recipe.match(player).canCraft && count < 8)
                    } else {
                        notifyFail(player)
                    }
                }

                // 丢弃一键合成全部
                ClickType.DROP, ClickType.CONTROL_DROP -> {
                    if (recipe.match(player).canCraft) {
                        do {
                            tryCraft(recipe, player)
                        } while (recipe.match(player).canCraft)
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

/**
 * 方便函数.
 */
private fun NekoStack.render0(): NekoStack {
    val context = CraftingStationContext(Pos.PREVIEW, erase = true)
    ItemRenderers.CRAFTING_STATION.render(this, context)
    return this
}