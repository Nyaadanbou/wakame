package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.reforge.merge.MergingTable
import cc.mewcraft.wakame.reforge.merge.SimpleMergingSession
import cc.mewcraft.wakame.util.hideTooltip
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle

internal class MergingMenu(
    val table: MergingTable,
    val viewer: Player,
) : KoinComponent {

    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.MERGE
    }

    /**
     * 给玩家展示合并台.
     */
    fun open() {
        primaryWindow.open()
    }

    /**
     * 基于当前 [session] 的状态, 执行一次合并操作.
     */
    fun executeReforge() {
        session.executeReforge()
    }

    /**
     * 根据当前 [session] 的状态, 刷新输出容器里的物品.
     */
    fun updateOutput() {
        // 获取最新的合并结果
        val result = session.latestResult
        // 根据合并的结果, 渲染输出容器里的物品
        val output = ResultRenderer.render(result)
        // 设置输出容器里的物品
        setOutputSlot(output)
    }

    /**
     * 本次合并的会话.
     *
     * ## 开发日记 2024/8/9
     * 不像 mod 和 reroll 的会话, merge 的会话是在打开菜单的时候创建.
     * 并且直到菜单关闭之前, 会话永远是这一个对象, 不会中途替换成其他的.
     * 而菜单这边的逻辑, 需要根据几个虚拟容器的变化, 来改变会话中的状态.
     */
    private val session: MergingSession = SimpleMergingSession(viewer, table)

    private val logger: Logger by inject()

    private val inputSlot1: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val inputSlot2: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1))

    private val primaryGui: Gui = Gui.normal { builder ->
        builder.setStructure(
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". a . b . . . c .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        )
        builder.addIngredient('.', SimpleItem(ItemStack(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('a', inputSlot1)
        builder.addIngredient('b', inputSlot2)
        builder.addIngredient('c', outputSlot, ItemWrapper(ItemStack(Material.BARRIER).hideTooltip(true)))
    }

    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(table.title)
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    init {
        inputSlot1.guiPriority = 3
        inputSlot2.guiPriority = 2
        outputSlot.guiPriority = 1

        inputSlot1.setPreUpdateHandler { e -> onInputSlotPreUpdate(e, InputSlot.INPUT1) }
        inputSlot2.setPreUpdateHandler { e -> onInputSlotPreUpdate(e, InputSlot.INPUT2) }
        outputSlot.setPreUpdateHandler(::onOutputSlotPreUpdate)
    }

    private enum class InputSlot {
        INPUT1, INPUT2
    }

    //<editor-fold desc="inventory listeners">
    private fun onInputSlotPreUpdate(e: ItemPreUpdateEvent, inputSlot: InputSlot) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        logger.info("$PREFIX Input slot ($inputSlot) pre-update: ${oldItem?.type} -> ${newItem?.type}")

        when {
            e.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                e.isCancelled = true
            }

            e.isAdd -> {
                val added = newItem?.tryNekoStack ?: run {
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    e.isCancelled = true; return
                }

                when (inputSlot) {
                    InputSlot.INPUT1 -> {
                        session.inputItem1 = added
                    }

                    InputSlot.INPUT2 -> {
                        session.inputItem2 = added
                    }
                }

                executeReforge()
                updateOutput()
            }

            e.isRemove -> {
                e.isCancelled = true

                when (inputSlot) {
                    InputSlot.INPUT1 -> {
                        setInputSlot1(null)
                        session.returnInputItem1(viewer)
                    }

                    InputSlot.INPUT2 -> {
                        setInputSlot2(null)
                        session.returnInputItem2(viewer)
                    }
                }

                executeReforge()
                updateOutput()
            }
        }
    }

    private fun onOutputSlotPreUpdate(e: ItemPreUpdateEvent) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        logger.info("$PREFIX Output slot pre-update: ${oldItem?.type} -> ${newItem?.type}")

        when {
            e.isSwap || e.isAdd -> {
                viewer.sendPlainMessage("猫咪不可以!")
                e.isCancelled = true
            }

            e.isRemove -> {
                e.isCancelled = true

                val result = session.latestResult
                if (result.successful) {

                    // 玩家必须有足够的资源
                    if (!result.cost.test(viewer)) {
                        return
                    }

                    // 把合并后的物品递给玩家
                    val handle = result.item.itemStack
                    viewer.inventory.addItem(handle)

                    // 清空菜单中的物品
                    setInputSlot1(null)
                    setInputSlot2(null)
                    setOutputSlot(null)

                    // 重置会话状态
                    session.reset()
                }
            }
        }
    }
    //</editor-fold>

    private fun onWindowClose() {
        logger.info("$PREFIX Menu closed for ${viewer.name}")

        setInputSlot1(null)
        setInputSlot2(null)
        setOutputSlot(null)

        session.returnInputItem1(viewer)
        session.returnInputItem2(viewer)
        session.frozen = true
    }

    private fun onWindowOpen() {
        logger.info("$PREFIX Menu opened for ${viewer.name}")
    }

    private fun getInputSlot1(): ItemStack? {
        return inputSlot1.getItem(0)
    }

    private fun setInputSlot1(item: ItemStack?) {
        inputSlot1.setItemSilently(0, item)
    }

    private fun getInputSlot2(): ItemStack? {
        return inputSlot2.getItem(0)
    }

    private fun setInputSlot2(item: ItemStack?) {
        inputSlot2.setItemSilently(0, item)
    }

    private fun getOutputSlot(): ItemStack? {
        return outputSlot.getItem(0)
    }

    private fun setOutputSlot(item: ItemStack?) {
        outputSlot.setItemSilently(0, item)
    }
}