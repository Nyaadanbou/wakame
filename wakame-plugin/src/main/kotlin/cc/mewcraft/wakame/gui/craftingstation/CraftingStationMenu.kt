package cc.mewcraft.wakame.gui.craftingstation

import cc.mewcraft.wakame.craftingstation.recipe.Recipe
import cc.mewcraft.wakame.craftingstation.recipe.RecipeMatcherResult
import cc.mewcraft.wakame.craftingstation.station.CraftingStation
import cc.mewcraft.wakame.craftingstation.station.CraftingStationSession
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import cc.mewcraft.wakame.item.resolveToItemWrapper
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.window.Window

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
        get() = station.primaryMenuSettings

    /**
     * 合成站的会话.
     * 玩家打开合成站便创建.
     * [CraftingStationSession] 类创建时会自动初始化, 无需额外手动刷新.
     */
    val stationSession = CraftingStationSession(station, viewer)

    /**
     * 合成站菜单的 [Gui].
     */
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
                .setItemProvider { _, _ ->
                    settings.getIcon("prev_page").resolveToItemWrapper()
                }
                .addClickHandler { _, gui, _ ->
                    gui.page -= 1
                }
        )
        .addIngredient(
            '>', BoundItem.pagedBuilder()
                .setItemProvider { _, _ ->
                    settings.getIcon("next_page").resolveToItemWrapper()
                }
                .addClickHandler { _, gui, _ ->
                    gui.page += 1
                }
        )
        .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        .build()

    /**
     * 合成站菜单的 [Window].
     */
    private val primaryWindowBuilder: Window.Builder.Normal.Split = Window.builder().apply {
        setUpperGui(primaryGui)
        addOpenHandler(::onWindowOpen)
        addCloseHandler { onWindowClose() }
    }

    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    /**
     * 刷新 Gui.
     * 根据 [CraftingStationSession] 的内容刷新展示配方的物品以及标题.
     */
    fun update() {
        // 排序已在 StationSession 的迭代器中实现
        primaryGui.setContent(stationSession.getRecipeMatcherResults().map(::createRecipeItem))
        primaryWindowBuilder.setTitle(settings.title)
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
        primaryWindowBuilder.open(viewer)
    }

    private fun onWindowOpen() {
        playerInventorySuppressor.startListening()
    }

    private fun onWindowClose() {
        playerInventorySuppressor.stopListening()
    }

    /**
     * 展示一个配方的图标.
     */
    private fun createRecipeItem(recipeMatcherResult: RecipeMatcherResult): Item {
        return Item.builder()
            .setItemProvider { _ ->
                ItemWrapper(recipeMatcherResult.getListingDisplay(settings))
            }
            .addClickHandler { _, click ->
                val clickType = click.clickType
                val player = click.player
                when (clickType) {
                    // 左键预览
                    ClickType.LEFT -> {
                        val menu = CraftingPreviewMenu(this, recipeMatcherResult.recipe, player)
                        menu.open()
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
                        /* do nothing */
                    }
                }
            }
            .build()
    }

    private fun updateMenu() {
        stationSession.updateRecipeMatcherResults()
        update()
    }
}

/**
 * 封装了合成逻辑的工具函数.
 */
internal fun tryCraft(recipe: Recipe, player: Player) {
    // 无法正常消耗则抛异常中断, 不给予玩家任何东西
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

internal fun notifyFail(player: Player) {
    player.sendMessage(text {
        content("你没有足够的材料来合成这个物品!")
        color(NamedTextColor.RED)
    })
}
