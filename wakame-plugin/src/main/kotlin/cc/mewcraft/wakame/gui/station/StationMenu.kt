package cc.mewcraft.wakame.gui.station

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.CraftingStationContext
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.station.Station
import cc.mewcraft.wakame.station.StationSession
import cc.mewcraft.wakame.station.recipe.RecipeMatcherResult
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
import xyz.xenondevs.invui.item.*
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.builder.setDisplayName
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

class StationMenu(
    /**
     * 该菜单所依赖的合成站.
     */
    val station: Station,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) : KoinComponent {
    private val logger: Logger by inject()

    /**
     * 该菜单的布局
     */
    private val layout: MenuLayout = station.layout

    /**
     * 合成站的会话.
     * 玩家打开合成站便创建.
     * [StationSession] 类创建时会自动初始化, 无需额外手动刷新.
     */
    val stationSession = StationSession(station, viewer)

    /**
     * 合成站菜单的 [Gui].
     * 'X': background
     * '.': recipe
     * '<': prev_page
     * '>': next_page
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*layout.structure.toTypedArray())
        builder.addIngredient('X', BackgroundItem(layout))
        builder.addIngredient('<', PrevItem(this))
        builder.addIngredient('>', NextItem(this))
        builder.addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }

    /**
     * 合成站菜单的 [Window].
     */
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
    }

    /**
     * 刷新Gui.
     * 根据 [StationSession] 的内容刷新展示配方的物品以及标题.
     */
    fun update() {
        // 排序已在 StationSession 的迭代器中实现
        val recipeItems = stationSession.map {
            RecipeItem(this, it)
        }
        primaryGui.setContent(recipeItems)
        val title = layout.title
        // TODO slot背景颜色红绿显示
        primaryWindow.setTitle(title)
    }

    /**
     * 向指定玩家打开合成站的主界面.
     */
    fun open() {
        primaryWindow.open(viewer)
    }

    /**
     * 初始化时刷新一次.
     */
    init {
        update()
    }

    /**
     * 背景占位的图标 [Item].
     */
    class BackgroundItem(
        private val layout: MenuLayout,
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            // TODO gui物品
            val key = layout.getIcon("background")
            val nekoStack = ItemRegistry.CUSTOM.find(key)
                ?.realize()
                ?.render()
                ?: return ItemWrapper(ItemStack(Material.GRAY_STAINED_GLASS_PANE).hideTooltip(true))
            return ItemWrapper(nekoStack.itemStack)
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            // do nothing
        }
    }

    /**
     * 上一页的图标 [Item].
     */
    class PrevItem(
        private val stationMenu: StationMenu,
    ) : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            // TODO gui物品
            val layout = stationMenu.layout
            if (!stationMenu.primaryGui.hasPreviousPage()) {
                return BackgroundItem(layout).itemProvider
            }
            val key = layout.getIcon("prev_page")
            val nekoStack = ItemRegistry.CUSTOM.find(key)
                ?.realize()
                ?.render()
                ?: return ItemBuilder(Material.SOUL_SAND).setDisplayName(Component.text("上一页").color(NamedTextColor.AQUA))
            return ItemWrapper(nekoStack.itemStack)
        }
    }

    /**
     * 下一页的图标 [Item].
     */
    class NextItem(
        private val stationMenu: StationMenu,
    ) : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            // TODO gui物品
            val layout = stationMenu.layout
            if (!stationMenu.primaryGui.hasNextPage()) {
                return BackgroundItem(layout).itemProvider
            }
            val key = layout.getIcon("next_page")
            val nekoStack = ItemRegistry.CUSTOM.find(key)
                ?.realize()
                ?.render()
                ?: return ItemBuilder(Material.MAGMA_BLOCK).setDisplayName(Component.text("下一页").color(NamedTextColor.AQUA))
            return ItemWrapper(nekoStack.itemStack)
        }
    }

    /**
     * 展示一个配方的图标 [Item].
     */
    class RecipeItem(
        private val stationMenu: StationMenu,
        private val recipeMatcherResult: RecipeMatcherResult,
    ) : AbstractCraftItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemWrapper(recipeMatcherResult.displayItemStack(stationMenu.layout))
        }

        private fun updateMenu() {
            // 刷新会话中的配方匹配结果
            stationMenu.stationSession.updateRecipeMatcherResults()
            // 刷新菜单Gui
            stationMenu.update()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                // 左键预览
                ClickType.LEFT -> {
                    PreviewMenu(recipeMatcherResult.recipe, player, stationMenu).open()
                }

                // 右键合成
                ClickType.RIGHT -> {
                    val stationRecipe = recipeMatcherResult.recipe
                    if (stationRecipe.match(player).canCraft) {
                        tryCraft(stationRecipe, player)
                    } else {
                        notifyFail(player)
                    }

                    updateMenu()
                }

                // 潜行合成8次
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                    val stationRecipe = recipeMatcherResult.recipe
                    var count = 0
                    if (stationRecipe.match(player).canCraft) {
                        do {
                            tryCraft(stationRecipe, player)
                            count++
                        } while (stationRecipe.match(player).canCraft && count < 8)
                    } else {
                        notifyFail(player)
                    }

                    updateMenu()
                }

                // 丢弃一键合成全部
                ClickType.DROP, ClickType.CONTROL_DROP -> {
                    val stationRecipe = recipeMatcherResult.recipe
                    if (stationRecipe.match(player).canCraft) {
                        do {
                            tryCraft(stationRecipe, player)
                        } while (stationRecipe.match(player).canCraft)
                    } else {
                        notifyFail(player)
                    }

                    updateMenu()
                }

                else -> {
                    // do nothing
                }
            }
        }

    }
}

/**
 * 封装了合成逻辑的一个抽象 [Item].
 */
abstract class AbstractCraftItem : AbstractItem() {
    fun tryCraft(stationRecipe: StationRecipe, player: Player) {
        // 无法正常执行消耗就抛出异常中断代码执行
        // 不给玩家执行合成的结果
        try {
            stationRecipe.consume(player)
            stationRecipe.output.apply(player)
        } catch (e: RuntimeException) {
            player.sendMessage(
                Component.text("发生了一个内部错误，请汇报给管理员!")
                    .color(NamedTextColor.RED)
            )
            e.printStackTrace()
        }
    }

    fun notifyFail(player: Player) {
        player.sendMessage(
            Component.text("你没有足够的材料来合成这个物品!")
                .color(NamedTextColor.RED)
        )
    }

}

private fun NekoStack.render(): NekoStack {
    val context = CraftingStationContext(CraftingStationContext.Pos.OVERVIEW)
    ItemRenderers.CRAFTING_STATION.render(this, context)
    return this
}
