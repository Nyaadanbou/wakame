package cc.mewcraft.wakame.gui.station

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext
import cc.mewcraft.wakame.display2.implementation.crafting_station.CraftingStationContext.*
import cc.mewcraft.wakame.gui.MenuLayout
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import cc.mewcraft.wakame.gui.toItemProvider
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.station.Station
import cc.mewcraft.wakame.station.StationSession
import cc.mewcraft.wakame.station.recipe.RecipeMatcherResult
import cc.mewcraft.wakame.station.recipe.StationRecipe
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.koin.core.component.KoinComponent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.*
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

internal class StationMenu(
    /**
     * 该菜单所依赖的合成站.
     */
    val station: Station,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) : KoinComponent {
    /**
     * 该菜单的布局
     */
    private val layout: MenuLayout
        get() = station.layout

    /**
     * 合成站的会话.
     * 玩家打开合成站便创建.
     * [StationSession] 类创建时会自动初始化, 无需额外手动刷新.
     */
    val stationSession = StationSession(station, viewer)

    /**
     * 合成站菜单的 [Gui].
     *
     * - `X`: background
     * - `.`: recipe
     * - `<`: prev_page
     * - `>`: next_page
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*layout.structure)
        builder.addIngredient('X', BackgroundItem())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('.', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }

    /**
     * 合成站菜单的 [Window].
     */
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
        addOpenHandler(::onWindowOpen)
        addCloseHandler(::onWindowClose)
    }

    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    /**
     * 刷新 Gui.
     * 根据 [StationSession] 的内容刷新展示配方的物品以及标题.
     */
    fun update() {
        // 排序已在 StationSession 的迭代器中实现
        primaryGui.setContent(stationSession.map(::RecipeItem))
        primaryWindow.setTitle(layout.title) // TODO slot 背景颜色红绿显示
    }

    /**
     * 初始化时刷新一次.
     */
    init {
        update()
    }

    /**
     * 向指定玩家打开合成站的主界面.
     */
    fun open() {
        primaryWindow.open(viewer)
    }

    private fun onWindowOpen() {
        playerInventorySuppressor.startListening()
    }

    private fun onWindowClose() {
        playerInventorySuppressor.stopListening()
    }

    /**
     * 背景占位的图标.
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
     * 上一页的图标.
     */
    inner class PrevItem : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return layout.getIcon("prev_page").render0().toItemProvider()
        }
    }

    /**
     * 下一页的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return layout.getIcon("next_page").render0().toItemProvider()
        }
    }

    /**
     * 展示一个配方的图标.
     */
    inner class RecipeItem(
        private val recipeMatcherResult: RecipeMatcherResult,
    ) : AbstractCraftItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemWrapper(recipeMatcherResult.displayItemStack(layout))
        }

        private fun updateMenu() {
            // 刷新会话中的配方匹配结果
            stationSession.updateRecipeMatcherResults()
            // 刷新菜单Gui
            update()
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                // 左键预览
                ClickType.LEFT -> {
                    PreviewMenu(recipeMatcherResult.recipe, player, this@StationMenu).open()
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
internal abstract class AbstractCraftItem : AbstractItem() {
    fun tryCraft(stationRecipe: StationRecipe, player: Player) {
        // 无法正常执行消耗就抛出异常中断代码执行
        // 不给玩家执行合成的结果
        try {
            stationRecipe.consume(player)
            stationRecipe.output.apply(player)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            player.sendMessage(text {
                content("发生了一个内部错误, 请汇报给管理员!")
                color(NamedTextColor.RED)
            })
        }
    }

    fun notifyFail(player: Player) {
        player.sendMessage(text {
            content("你没有足够的材料来合成这个物品!")
            color(NamedTextColor.RED)
        })
    }
}

/**
 * 方便函数.
 */
private fun NekoStack.render0(): NekoStack {
    val context = CraftingStationContext(Pos.OVERVIEW, erase = true)
    ItemRenderers.CRAFTING_STATION.render(this, context)
    return this
}
