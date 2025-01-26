package cc.mewcraft.wakame.gui.craftingstation

import cc.mewcraft.wakame.craftingstation.CraftingStation
import cc.mewcraft.wakame.craftingstation.CraftingStationSession
import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.craftingstation.recipe.RecipeMatcherResult
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

internal class CraftingStationMenu(
    /**
     * 该菜单所依赖的合成站.
     */
    val station: CraftingStation,

    /**
     * 该菜单的用户, 也就是正在查看该菜单的玩家.
     */
    val viewer: Player,
) {
    /**
     * 该菜单的布局
     */
    private val settings: BasicMenuSettings
        get() = station.primaryLayout

    /**
     * 合成站的会话.
     * 玩家打开合成站便创建.
     * [CraftingStationSession] 类创建时会自动初始化, 无需额外手动刷新.
     */
    val stationSession = CraftingStationSession(station, viewer)

    /**
     * 合成站菜单的 [Gui].
     *
     * - `x`: background
     * - `.`: recipe
     * - `<`: prev_page
     * - `>`: next_page
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*settings.structure)
        builder.addIngredient('x', BackgroundItem())
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
     * 根据 [CraftingStationSession] 的内容刷新展示配方的物品以及标题.
     */
    fun update() {
        // 排序已在 StationSession 的迭代器中实现
        primaryGui.setContent(stationSession.getRecipeMatcherResults().map(::RecipeItem))
        primaryWindow.setTitle(settings.title) // TODO slot 背景颜色红绿显示
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
            return settings.getSlotDisplay("background").resolveToItemWrapper()
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
            return settings.getSlotDisplay("prev_page").resolveToItemWrapper()
        }
    }

    /**
     * 下一页的图标.
     */
    inner class NextItem : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            return settings.getSlotDisplay("next_page").resolveToItemWrapper()
        }
    }

    /**
     * 展示一个配方的图标.
     */
    inner class RecipeItem(
        private val recipeMatcherResult: RecipeMatcherResult,
    ) : AbstractCraftItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemWrapper(recipeMatcherResult.getListingDisplay(settings))
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
                    CraftingPreviewMenu(this@CraftingStationMenu, recipeMatcherResult.recipe, player).open()
                }

                // 右键合成
                ClickType.RIGHT -> {
                    val stationRecipe = recipeMatcherResult.recipe
                    if (stationRecipe.match(player).isAllowed) {
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
                    if (stationRecipe.match(player).isAllowed) {
                        do {
                            tryCraft(stationRecipe, player)
                            count++
                        } while (stationRecipe.match(player).isAllowed && count < 8)
                    } else {
                        notifyFail(player)
                    }

                    updateMenu()
                }

                // 丢弃一键合成全部
                ClickType.DROP, ClickType.CONTROL_DROP -> {
                    val stationRecipe = recipeMatcherResult.recipe
                    if (stationRecipe.match(player).isAllowed) {
                        do {
                            tryCraft(stationRecipe, player)
                        } while (stationRecipe.match(player).isAllowed)
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
    fun tryCraft(recipe: Recipe, player: Player) {
        // 无法正常执行消耗就抛出异常中断代码执行, 不给予玩家任何东西.
        try {
            recipe.consume(player)
            recipe.output.apply(player)
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
