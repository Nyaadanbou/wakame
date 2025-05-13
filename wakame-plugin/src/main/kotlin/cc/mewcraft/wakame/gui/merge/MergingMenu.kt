package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.merging_table.MergingTableContext
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import cc.mewcraft.wakame.item2.isKoish
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.reforge.merge.MergingSession
import cc.mewcraft.wakame.reforge.merge.MergingTable
import cc.mewcraft.wakame.reforge.merge.SimpleMergingSession
import cc.mewcraft.wakame.util.decorate
import cc.mewcraft.wakame.util.item.fastLoreOrEmpty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle
import kotlin.properties.Delegates

internal class MergingMenu(
    val table: MergingTable,
    val viewer: Player,
) {

    /**
     * 给玩家展示合并台.
     */
    fun open() {
        primaryWindow.open()
        viewer.sendMessage(TranslatableMessages.MSG_OPENED_MERGING_MENU)
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

    private val logger: Logger = LOGGER.decorate(prefix = ReforgingStationConstants.MERING_LOG_PREFIX)

    private val inputSlot1: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        guiPriority = 3
        setPreUpdateHandler { e -> onInputSlotPreUpdate(e, InputSlot.INPUT1) }
    }
    private val inputSlot2: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        guiPriority = 2
        setPreUpdateHandler { e -> onInputSlotPreUpdate(e, InputSlot.INPUT2) }
    }
    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        guiPriority = 1
        setPreUpdateHandler(::onOutputSlotPreUpdate)
    }

    private val primaryGui: Gui = Gui.normal { builder ->
        builder.setStructure(*table.primaryMenuSettings.structure)
        builder.addIngredient('.', table.primaryMenuSettings.getSlotDisplay("background").resolveToItemWrapper())
        builder.addIngredient('a', inputSlot1)
        builder.addIngredient('b', inputSlot2)
        builder.addIngredient('c', outputSlot, table.primaryMenuSettings.getSlotDisplay("output_empty").resolveToItemWrapper())
    }

    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(table.primaryMenuSettings.title)
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    /**
     * 玩家是否已经确认取出合并后的物品.
     * 这只是个标记, 具体的作用取决于实现.
     */
    private var confirmed: Boolean by Delegates.observable(false) { _, old, new ->
        // logger.info("Confirmed status updated: $old -> $new")
    }

    init {
        executeReforge() // 初始化时执行一次空的操作
        updateOutputSlot()
    }

    private enum class InputSlot {
        INPUT1, INPUT2
    }

    //<editor-fold desc="inventory listeners">
    private fun onInputSlotPreUpdate(e: ItemPreUpdateEvent, inputSlot: InputSlot) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        // logger.info("Input slot ($inputSlot) pre-update: ${oldItem?.type} -> ${newItem?.type}")

        when {
            e.isSwap -> {
                e.isCancelled = true
                viewer.sendMessage(TranslatableMessages.MSG_ERR_CANCELLED)
            }

            e.isAdd -> {
                val added = newItem?.takeIf { it.isKoish } ?: run {
                    e.isCancelled = true
                    viewer.sendMessage(TranslatableMessages.MSG_ERR_NOT_AUGMENT_CORE)
                    return
                }

                when (inputSlot) {
                    InputSlot.INPUT1 -> {
                        session.inputItem1 = added
                    }

                    InputSlot.INPUT2 -> {
                        session.inputItem2 = added
                    }
                }

                // 重新渲染放入容器的物品
                e.newItem = renderInputSlot(added)

                confirmed = false
                executeReforge()
                updateOutputSlot()
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

                confirmed = false
                executeReforge()
                updateOutputSlot()
            }
        }
    }

    private fun onOutputSlotPreUpdate(e: ItemPreUpdateEvent) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        // logger.info("Output slot pre-update: ${oldItem?.type} -> ${newItem?.type}")

        when {
            e.isSwap || e.isAdd -> {
                e.isCancelled = true
                viewer.sendMessage(TranslatableMessages.MSG_ERR_CANCELLED)
            }

            e.isRemove -> {
                e.isCancelled = true
                val reforgeResult = session.latestResult
                if (reforgeResult.isSuccess) {

                    if (!confirmed) {
                        confirmed = true
                        updateOutputSlot()
                        return
                    }

                    // 玩家必须有足够的资源
                    if (!reforgeResult.reforgeCost.test(viewer)) {
                        setOutputSlot(table.primaryMenuSettings.getSlotDisplay("output_insufficient_resource").resolveToItemStack())
                        return
                    }

                    // 从玩家扣除所需的资源
                    reforgeResult.reforgeCost.take(viewer)

                    // 把合并后的物品递给玩家
                    viewer.inventory.addItem(*session.getFinalOutputs())

                    // 清空菜单中的物品
                    setInputSlot1(null)
                    setInputSlot2(null)
                    setOutputSlot(null)

                    // 重置会话状态
                    session.reset()
                    confirmed = false
                }
            }
        }
    }
    //</editor-fold>


    /**
     * 基于当前 [session] 的状态, 执行一次合并操作.
     */
    private fun executeReforge() {
        session.executeReforge()
    }

    /**
     * 基于当前 [session] 的状态, 渲染物品 [source].
     */
    private fun renderInputSlot(source: ItemStack): ItemStack {
        val context = MergingTableContext.MergeInputSlot(session)
        ItemRenderers.MERGING_TABLE.render(source, context)
        return source.clone()
    }

    /**
     * 根据当前 [session] 的状态, 刷新输出容器里的物品.
     */
    private fun updateOutputSlot() {
        // 获取最新的合并结果
        val result = session.latestResult
        // 根据合并的结果, 渲染输出容器里的物品
        val output = renderOutputSlot(result)
        // 设置输出容器里的物品
        setOutputSlot(output)
    }

    /**
     * 负责渲染合并后的物品在 [MergingMenu.outputSlot] 里面的样子.
     */
    private fun renderOutputSlot(result: MergingSession.ReforgeResult): ItemStack {
        if (result.isSuccess) {
            // 获取合并成功后的核心, 用作基础物品堆叠
            val output = result.output

            // 使用合并工作台的渲染器渲染合并后的物品
            ItemRenderers.MERGING_TABLE.render(output, MergingTableContext.MergeOutputSlot(session))

            val slotDisplayId = if (confirmed) "output_ok_confirmed" else "output_ok_unconfirmed"
            val slotDisplayResolved = table.primaryMenuSettings.getSlotDisplay(slotDisplayId).resolveEverything {
                folded("item_lore", output.fastLoreOrEmpty)
                folded("type_description", result.reforgeType.description)
                folded("cost_description", result.reforgeCost.description)
                folded("result_description", result.description)
            }

            return slotDisplayResolved.applyTo(output)
        } else {
            return table.primaryMenuSettings.getSlotDisplay("output_failure").resolveToItemStack {
                // 这里仅仅解析 result_description 告诉玩家为什么合并失败.
                // 其他的信息, 比如[合并类型]没有必要在合并失败的时候显示出来.
                folded("result_description", result.description)
            }
        }
    }

    private fun onWindowOpen() {
        playerInventorySuppressor.startListening()

        logger.info("Merging window opened for ${viewer.name}")
    }

    private fun onWindowClose() {
        playerInventorySuppressor.stopListening()

        logger.info("Merging window closed for ${viewer.name}")

        setInputSlot1(null)
        setInputSlot2(null)
        setOutputSlot(null)

        session.returnInputItem1(viewer)
        session.returnInputItem2(viewer)
        session.frozen = true
    }

    @Suppress("SameParameterValue")
    private fun setInputSlot1(item: ItemStack?) {
        inputSlot1.setItemSilently(0, item)
    }

    @Suppress("SameParameterValue")
    private fun setInputSlot2(item: ItemStack?) {
        inputSlot2.setItemSilently(0, item)
    }

    private fun setOutputSlot(item: ItemStack?) {
        outputSlot.setItemSilently(0, item)
    }
}