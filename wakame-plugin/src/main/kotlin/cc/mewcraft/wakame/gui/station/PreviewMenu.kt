package cc.mewcraft.wakame.gui.station

import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.item.setSystemUse
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.station.recipe.StationRecipe
import cc.mewcraft.wakame.util.hideTooltip
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.builder.setDisplayName
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

/**
 * 合成站配方预览的菜单
 * 这个菜单是"静态"的
 * 即不涉及物品和标题的变化
 */
class PreviewMenu(
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
    val previousStationMenu: StationMenu
) : KoinComponent {
    private val logger: Logger by inject()

    /**
     * 该菜单的布局
     */
    private val layout: MenuLayout = previousStationMenu.station.previewLayout

    /**
     * 合成站配方预览菜单的 [Gui].
     * 'X': background
     * 'I': input/choices
     * 'O': output/result
     * '<': prev_page
     * '>': next_page
     * 'C': craft
     * 'B': back
     */
    private val previewGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*layout.structure.toTypedArray())
        builder.addIngredient('X', BackgroundItem(layout))
        builder.addIngredient('<', PrevItem(this))
        builder.addIngredient('>', NextItem(this))
        builder.addIngredient('C', CraftItem(this, recipe))
        builder.addIngredient('B', BackItem(this))
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
     * 背景占位的图标 [Item].
     */
    class BackgroundItem(
        private val layout: MenuLayout
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            //TODO gui物品
            val key = layout.getIcon("background")
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemWrapper(ItemStack(Material.GRAY_STAINED_GLASS_PANE).hideTooltip(true))
            nekoStack.setSystemUse()
            return ItemWrapper(nekoStack.itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            //do nothing
        }
    }

    /**
     * 上一页的图标 [Item].
     */
    class PrevItem(
        private val previewMenu: PreviewMenu,
    ) : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            //TODO gui物品
            val layout = previewMenu.layout
            if (!previewMenu.previewGui.hasPreviousPage()) {
                return BackgroundItem(layout).itemProvider
            }
            val key = layout.getIcon("prev_page")
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemBuilder(Material.SOUL_SAND)
                .setDisplayName(Component.text("上一页").color(NamedTextColor.AQUA))
            nekoStack.setSystemUse()
            return ItemWrapper(nekoStack.itemStack)
        }
    }

    /**
     * 下一页的图标 [Item].
     */
    class NextItem(
        private val previewMenu: PreviewMenu,
    ) : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            //TODO gui物品
            val layout = previewMenu.layout
            if (!previewMenu.previewGui.hasNextPage()) {
                return BackgroundItem(layout).itemProvider
            }
            val key = layout.getIcon("next_page")
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemBuilder(Material.MAGMA_BLOCK)
                .setDisplayName(Component.text("下一页").color(NamedTextColor.AQUA))
            nekoStack.setSystemUse()
            return ItemWrapper(nekoStack.itemStack)
        }
    }

    /**
     * 返回的图标 [Item].
     */
    class BackItem(
        private val previewMenu: PreviewMenu
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            //TODO gui物品
            val layout = previewMenu.layout
            val key = layout.getIcon("back")
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemBuilder(Material.BARRIER)
                .setDisplayName(Component.text("返回").color(NamedTextColor.AQUA))
            nekoStack.setSystemUse()
            return ItemWrapper(nekoStack.itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            val previousStationMenu = previewMenu.previousStationMenu
            previousStationMenu.stationSession.updateRecipeMatcherResults()
            previousStationMenu.update()
            previousStationMenu.open()
        }
    }

    /**
     * 触发点击合成的图标 [Item].
     */
    class CraftItem(
        private val previewMenu: PreviewMenu,
        private var stationRecipe: StationRecipe
    ) : AbstractCraftItem() {
        override fun getItemProvider(): ItemProvider {
            //TODO gui物品
            val layout = previewMenu.layout
            val key = layout.getIcon("craft")
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemBuilder(Material.CRAFTING_TABLE)
                .setDisplayName(Component.text("合成").color(NamedTextColor.YELLOW))
            nekoStack.setSystemUse()
            return ItemWrapper(nekoStack.itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                // 单击合成
                ClickType.LEFT, ClickType.RIGHT -> {
                    if (stationRecipe.match(player).canCraft) {
                        tryCraft(stationRecipe, player)
                    } else {
                        notifyFail(player)
                    }
                }

                // 潜行合成8次
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                    var count = 0
                    if (stationRecipe.match(player).canCraft) {
                        do {
                            tryCraft(stationRecipe, player)
                            count++
                        } while (stationRecipe.match(player).canCraft && count < 8)
                    } else {
                        notifyFail(player)
                    }
                }

                // 丢弃一键合成全部
                ClickType.DROP, ClickType.CONTROL_DROP -> {
                    if (stationRecipe.match(player).canCraft) {
                        do {
                            tryCraft(stationRecipe, player)
                        } while (stationRecipe.match(player).canCraft)
                    } else {
                        notifyFail(player)
                    }
                }

                else -> {
                    //do nothing
                }
            }
        }

    }
}