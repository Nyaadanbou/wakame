package cc.mewcraft.wakame.gui.station

import cc.mewcraft.wakame.item.realize
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.station.*
import cc.mewcraft.wakame.user.User
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
    val user: User<Player>,
) : KoinComponent {
    private val logger: Logger by inject()

    /**
     * 合成站的会话.
     * 玩家打开合成站便创建.
     * [StationSession] 类创建时会自动初始化, 无需额外手动刷新.
     */
    private val stationSession = StationSession(station, user)

    /**
     * 合成站菜单的 [Gui].
     * 'X': background
     * '.': recipe
     * '<': prev_page
     * '>': next_page
     */
    private val primaryGui: PagedGui<Item> = PagedGui.items { builder ->
        builder.setStructure(*station.layout.structure.toTypedArray())
        builder.addIngredient('X', BackgroundItem(station.layout))
        builder.addIngredient('<', PrevItem(station.layout))
        builder.addIngredient('>', NextItem(station.layout))
        builder.addIngredient('.', Markers.CONTENT_LIST_SLOT_VERTICAL)
    }

    /**
     * 合成站菜单的 [Window].
     */
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
    }

    /**
     * 根据 [StationSession] 的内容刷新Gui中展示配方的物品以及标题.
     */
    fun update() {
        // 排序已在 StationSession 的迭代器中实现
        val recipeItems = stationSession.map {
            RecipeItem(this, it)
        }
        primaryGui.setContent(recipeItems)
        val title = station.layout.title
        //TODO slot背景颜色红绿显示
        primaryWindow.setTitle(title)
    }

    /**
     * 向指定玩家打开工作站的主界面.
     */
    fun open() {
        primaryWindow.open(user.player)
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
        private val stationLayout: StationLayout
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            //TODO 使用gui物品
            val key = stationLayout.icons["background"]
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemWrapper(ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE).hideTooltip(true))
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
        private val stationLayout: StationLayout
    ) : PageItem(false) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            //TODO 使用gui物品
            val key = stationLayout.icons["prev_page"]
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemBuilder(Material.SOUL_SAND)
                .setDisplayName(Component.text("上一页").color(NamedTextColor.AQUA))
            return ItemWrapper(nekoStack.itemStack)
        }
    }

    /**
     * 下一页的图标 [Item].
     */
    class NextItem(
        private val stationLayout: StationLayout
    ) : PageItem(true) {
        override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
            //TODO 使用gui物品
            val key = stationLayout.icons["next_page"]
            val nekoStack = ItemRegistry.CUSTOM.find(key)?.realize()
            nekoStack ?: return ItemBuilder(Material.MAGMA_BLOCK)
                .setDisplayName(Component.text("下一页").color(NamedTextColor.AQUA))
            return ItemWrapper(nekoStack.itemStack)
        }
    }

    /**
     * 配方的图标 [Item].
     */
    class RecipeItem(
        private val stationMenu: StationMenu,
        private val recipeMatcherResult: RecipeMatcherResult
    ) : AbstractItem() {
        override fun getItemProvider(): ItemProvider {
            return ItemWrapper(recipeMatcherResult.guiItemStack(stationMenu.station.layout))
        }

        override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
            when (clickType) {
                // 左键合成
                ClickType.LEFT -> {
                    clickToCraft()
                }

                // 右键预览
                ClickType.RIGHT -> {
                    clickToPreview()
                }

                // 潜行一键合成全部
                ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> {
                    clickToCraftAll()
                }

                else -> {
                    //do nothing
                }
            }
        }

        private fun tryCraft(stationRecipe: StationRecipe, player: Player) {
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

        private fun notifyFail(player: Player) {
            player.sendMessage(
                Component.text("你没有足够的材料来合成这个物品!")
                    .color(NamedTextColor.RED)
            )
        }

        private fun clickToCraft() {
            val player = stationMenu.user.player
            val stationRecipe = recipeMatcherResult.recipe
            if (stationRecipe.match(player).canCraft) {
                tryCraft(stationRecipe, player)
            } else {
                notifyFail(player)
            }

            // 刷新会话中的配方匹配结果
            stationMenu.stationSession.updateRecipeMatcherResults()
            // 刷新菜单Gui
            stationMenu.update()
            // 通知 Windows
            notifyWindows()
        }

        private fun clickToPreview() {
            // TODO clickToPreview
        }

        private fun clickToCraftAll() {
            val player = stationMenu.user.player
            val stationRecipe = recipeMatcherResult.recipe
            if (stationRecipe.match(player).canCraft) {
                do {
                    tryCraft(stationRecipe, player)
                } while (stationRecipe.match(player).canCraft)
            } else {
                notifyFail(player)
            }

            // 刷新会话中的配方匹配结果
            stationMenu.stationSession.updateRecipeMatcherResults()
            // 刷新菜单Gui
            stationMenu.update()
            // 通知 Windows
            notifyWindows()
        }

    }
}