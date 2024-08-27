package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.reforge.reroll.RerollingTable
import cc.mewcraft.wakame.reforge.reroll.SimpleRerollingSession
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
     * 基于 [session] 的当前状态执行一次重造.
     */
    fun executeReforge() {
        session.executeReforge()
    }

    /**
     * 基于 [session] 的当前状态刷新输出容器.
     */
    fun updateOutput() {
        val output = ResultRenderer.render(session)
        setOutputSlot(output)
    }

    /**
     * 本菜单的 [RerollingSession].
     */
    val session: RerollingSession = SimpleRerollingSession(table, viewer)

    private val logger: Logger by inject()

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
        builder.addIngredient('o', outputSlot, ItemWrapper(ItemStack(Material.BARRIER).hideTooltip(true)))
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

    init {
        inputSlot.setPreUpdateHandler(::onInputInventoryPreUpdate)
        outputSlot.setPreUpdateHandler(::onOutputInventoryPreUpdate)
    }

    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("$PREFIX Input slot updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 inputSlot 里面的物品:
            event.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家尝试把物品放进 inputSlot:
            event.isAdd -> {

                // ... 说明玩家想要开始一次重造流程

                val ns = newItem?.tryNekoStack ?: run {
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    event.isCancelled = true; return
                }

                // 给会话输入源物品
                session.sourceItem = ns

                // 创建并设置子菜单
                setSelectionGuis(createSelectionGuis())

                // 更新输出容器
                updateOutput()
            }

            // 玩家尝试从 inputSlot 拿出物品:
            event.isRemove -> {

                // ... 说明玩家想中途放弃这次重造

                event.isCancelled = true

                // 归还玩家输入的所有物品
                val itemsToReturn = session.getAllPlayerInputs()
                viewer.inventory.addItem(*itemsToReturn.toTypedArray())

                // 重置会话的状态
                session.reset()

                // 清空菜单内容
                setInputSlot(null)
                setOutputSlot(null)
                setSelectionGuis(null)
            }
        }
    }

    private fun onOutputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("$PREFIX Output item updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 outputSlot 里面的物品:
            event.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家尝试把物品放进 outputSlot:
            event.isAdd -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家尝试从 outputSlot 拿出物品:
            event.isRemove -> {

                // ... 说明玩家想要完成这次重造

                event.isCancelled = true

                if (session.sourceItem == null) {
                    logger.error("$PREFIX An item is being removed from the output slot, but the source item is null. This is a bug!")
                    return
                }

                // 首先获得当前的重造结果
                val result = session.latestResult
                if (!result.successful) {
                    return
                }

                // 再次更新输出容器
                // 因为此时玩家所持有的资源可能发生了变化 (例如突然收到了一笔金币)
                updateOutput()

                // 判断玩家是否有足够的资源
                if (!result.cost.test(viewer)) {
                    return
                }

                // 把重造后的源物品给玩家
                viewer.inventory.addItem(result.item.itemStack)

                // 重置会话状态
                session.reset()

                // 清空菜单内容
                setInputSlot(null)
                setOutputSlot(null)
                setSelectionGuis(null)
            }
        }
    }

    private fun onWindowClose() {
        logger.info("$PREFIX Rerolling window closed for ${viewer.name}")

        setInputSlot(null)
        setOutputSlot(null)
        setSelectionGuis(null)

        val itemsToReturn = session.getAllPlayerInputs()
        viewer.inventory.addItem(*itemsToReturn.toTypedArray())

        session.reset()
        session.frozen = true
    }

    private fun onWindowOpen() {
        logger.info("$PREFIX RerollingMenu opened for ${viewer.name}")
    }

    private fun createSelectionGuis(): List<Gui> {
        return session.selectionMap
            .map { (_, sel) -> SelectionMenu(this, sel) }
            .map { it.primaryGui }
    }

    private fun setSelectionGuis(guis: List<Gui>?) {
        primaryGui.setContent(guis)
    }

    private fun setInputSlot(stack: ItemStack?) {
        inputSlot.setItemSilently(0, stack)
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