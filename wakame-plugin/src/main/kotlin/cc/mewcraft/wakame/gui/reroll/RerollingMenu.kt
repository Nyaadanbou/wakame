package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.reforge.reroll.RerollingTable
import cc.mewcraft.wakame.util.hideTooltip
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle
import kotlin.properties.Delegates

/**
 * 重造台的主菜单, 也是玩家打开重造台后的代码入口.
 */
internal class RerollingMenu(
    val table: RerollingTable,
    val viewer: Player,
) : KoinComponent {

    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.REROLL
    }

    /**
     * 给玩家显示 GUI.
     */
    fun open() {
        primaryWindow.open()
    }

    /**
     * 基于 [rerollingSession] 的当前状态执行一次重造, 并刷新输出容器.
     */
    fun refreshOutput() {
        val session = rerollingSession ?: run {
            logger.info("$PREFIX Trying to refresh output without a session.")
            return
        }

        if (session.frozen) {
            logger.error("$PREFIX Trying to refresh output in a frozen session. This is a bug!")
            return
        }

        // 执行一次重造
        val result = session.reforge()
        // 渲染输出物品
        val output = ResultRenderer.render(result)
        // 填充输出容器
        setOutputSlot(output)
    }

    /**
     * 当前正在进行中的重造.
     *
     * 初始值为 `null`, 表示当前没有正在进行的重造.
     * 当玩家放入一个物品到输入容器时, 会创建一个新的 [RerollingSession] 实例,
     * 并将其赋值给这个属性.
     */
    var rerollingSession: RerollingSession? by Delegates.observable(null) { _, old, new ->
        logger.info("$PREFIX Session status updated: $old -> $new")
    }

    val logger: Logger by inject()

    //<editor-fold desc="InvUI components">
    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val primaryGui: ScrollGui<Gui> = ScrollGui.guis { builder ->
        builder.setStructure(
            ". . . . . . . . .",
            ". . x x x x x . .",
            ". < x x x x x > .",
            ". . . . . . . . .",
            ". . i . . . o . .",
            ". . . . . . . . .",
        )
        builder.addIngredient('.', SimpleItem(ItemStack(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('i', inputSlot)
        builder.addIngredient('o', outputSlot)
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_VERTICAL)
    }
    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(table.title)
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }
    //</editor-fold>

    init {
        inputSlot.setPreUpdateHandler(::onInputInventoryPreUpdate)
        outputSlot.setPreUpdateHandler(::onOutputInventoryPreUpdate)
    }

    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("$PREFIX Input slot updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家交换物品:
            // 禁止该操作.
            event.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家把物品放进输入容器:
            // 这时候我们需要创建一个新的‘重造会话’,
            // 然后把所有(可重造的)词条栏图标呈现在菜单里,
            // 最后刷新一下输出容器里的图标.
            event.isAdd -> {
                val ns = newItem?.tryNekoStack ?: run {
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    event.isCancelled = true; return
                }

                if (!ns.components.has(ItemComponentTypes.LEVEL)) {
                    viewer.sendPlainMessage("请放入一个拥有等级的物品!")
                    event.isCancelled = true; return
                }

                if (!ns.components.has(ItemComponentTypes.CELLS)) {
                    viewer.sendPlainMessage("请放入一个拥有词条栏的物品!")
                    event.isCancelled = true; return
                }

                val session = RerollingSessionFactory.create(this, ns) ?: run {
                    viewer.sendPlainMessage("该物品不支持重造!")
                    event.isCancelled = true; return
                }

                // 设置会话
                rerollingSession = session

                // 将*克隆*放入输入容器
                event.newItem = ns.itemStack

                val selectionMenus = session.selections.map { (_, selection) ->
                    SelectionMenu(this, selection)
                }
                val selectionGuis = selectionMenus.map {
                    it.primaryGui
                }
                setSelectionGuis(selectionGuis)

                // 更新输出容器
                refreshOutput()
            }

            // 玩家从输入容器拿出物品:
            // 说明玩家中途放弃了这次重造.
            event.isRemove -> {
                event.isCancelled = true

                val session = rerollingSession ?: run {
                    logger.error("$PREFIX Rerolling session (viewer: ${viewer.name}) is null, but input item is being removed. This is a bug!")
                    return
                }

                setInputSlot(null)
                setOutputSlot(null)
                setSelectionGuis(null)

                session.returnInput(viewer)

                session.frozen = true
                rerollingSession = null
            }
        }
    }

    private fun onOutputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("$PREFIX Output item updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换物品:
            // 禁止该操作.
            event.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家把物品放进输出容器:
            // 禁止该操作.
            event.isAdd -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家从输出容器拿出物品:
            // 说明玩家想要完成这次重造.
            event.isRemove -> {
                event.isCancelled = true

                val session = rerollingSession ?: run {
                    logger.error("$PREFIX Rerolling session (viewer: ${viewer.name}) is null, but output item is being removed. This is a bug!")
                    return
                }

                // 首先获得当前的重造结果
                val result = session.result
                if (!result.cost.test(viewer)) {
                    viewer.sendPlainMessage("你没有足够的货币!")
                    return
                }

                // 把重造后的物品给玩家
                viewer.inventory.addItem(result.item.unsafe.handle)

                setInputSlot(null)
                setOutputSlot(null)
                setSelectionGuis(null)

                session.frozen = true
                rerollingSession = null
            }
        }
    }

    private fun onWindowClose() {
        logger.info("Rerolling window closed for ${viewer.name}")

        val session = rerollingSession ?: run {
            logger.info("$PREFIX The window is closed while session being null.")
            return
        }

        session.frozen = true
        rerollingSession = null

        setInputSlot(null)
        setOutputSlot(null)
        setSelectionGuis(null)
        session.returnInput(viewer)
    }

    private fun onWindowOpen() {
        logger.info("RerollingMenu opened for ${viewer.name}")
    }

    private fun setSelectionGuis(guis: List<Gui>?) {
        primaryGui.setContent(guis)
    }

    private fun setInputSlot(stack: ItemStack?) {
        inputSlot.setItemSilently(0, null)
    }

    private fun setOutputSlot(stack: ItemStack?) {
        outputSlot.setItemSilently(0, stack)
    }

    private class PrevItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { it.itemName(text("上一页")) }
            return ItemWrapper(stack)
        }
    }

    private class NextItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { it.itemName(text("下一页")) }
            return ItemWrapper(stack)
        }
    }
}