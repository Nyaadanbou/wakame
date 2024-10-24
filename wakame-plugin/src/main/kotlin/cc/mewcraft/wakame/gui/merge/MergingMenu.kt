package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.merging_table.MergingTableContext
import cc.mewcraft.wakame.gui.common.GuiMessages
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.customNeko
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.merge.*
import cc.mewcraft.wakame.util.*
import me.lucko.helper.text3.mini
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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

    /**
     * 给玩家展示合并台.
     */
    fun open() {
        primaryWindow.open()
    }

    /**
     * 基于当前 [session] 的状态, 执行一次合并操作.
     */
    private fun executeReforge() {
        session.executeReforge()
    }

    /**
     * 基于当前 [session] 的状态, 渲染物品 [source].
     */
    private fun renderInputSlot(source: NekoStack): ItemStack {
        val context = MergingTableContext.MergeInputSlot(session)
        ItemRenderers.MERGING_TABLE.render(source, context)
        return source.itemStack
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
     * 本次合并的会话.
     *
     * ## 开发日记 2024/8/9
     * 不像 mod 和 reroll 的会话, merge 的会话是在打开菜单的时候创建.
     * 并且直到菜单关闭之前, 会话永远是这一个对象, 不会中途替换成其他的.
     * 而菜单这边的逻辑, 需要根据几个虚拟容器的变化, 来改变会话中的状态.
     */
    private val session: MergingSession = SimpleMergingSession(viewer, table)

    private val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.MERGE)

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

    private enum class InputSlot {
        INPUT1, INPUT2
    }

    //<editor-fold desc="inventory listeners">
    private fun onInputSlotPreUpdate(e: ItemPreUpdateEvent, inputSlot: InputSlot) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        logger.info("Input slot ($inputSlot) pre-update: ${oldItem?.type} -> ${newItem?.type}")

        when {
            e.isSwap -> {
                viewer.sendMessage(GuiMessages.MESSAGE_CANCELLED)
                e.isCancelled = true
            }

            e.isAdd -> {
                val added = newItem?.customNeko ?: run {
                    viewer.sendMessage(text { content("请放入一个核心!"); color(NamedTextColor.RED) })
                    e.isCancelled = true
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

                executeReforge()
                updateOutputSlot()
            }
        }
    }

    private fun onOutputSlotPreUpdate(e: ItemPreUpdateEvent) {
        val oldItem = e.previousItem
        val newItem = e.newItem
        logger.info("Output slot pre-update: ${oldItem?.type} -> ${newItem?.type}")

        when {
            e.isSwap || e.isAdd -> {
                viewer.sendMessage(GuiMessages.MESSAGE_CANCELLED)
                e.isCancelled = true
            }

            e.isRemove -> {
                e.isCancelled = true

                val result = session.latestResult
                if (result.isSuccess) {

                    // 玩家必须有足够的资源
                    if (!result.reforgeCost.test(viewer)) {
                        setOutputSlot(ItemStack.of(Material.BARRIER).edit {
                            itemName = text { content("资源不足!"); color(NamedTextColor.RED) }
                        })
                        return
                    }

                    // 把合并后的物品递给玩家
                    viewer.inventory.addItem(*session.getFinalOutputs())

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
        logger.info("Menu closed for ${viewer.name}")

        setInputSlot1(null)
        setInputSlot2(null)
        setOutputSlot(null)

        session.returnInputItem1(viewer)
        session.returnInputItem2(viewer)
        session.frozen = true
    }

    private fun onWindowOpen() {
        logger.info("Menu opened for ${viewer.name}")
    }

    private fun getInputSlot1(): ItemStack? {
        return inputSlot1.getItem(0)
    }

    @Suppress("SameParameterValue")
    private fun setInputSlot1(item: ItemStack?) {
        inputSlot1.setItemSilently(0, item)
    }

    private fun getInputSlot2(): ItemStack? {
        return inputSlot2.getItem(0)
    }

    @Suppress("SameParameterValue")
    private fun setInputSlot2(item: ItemStack?) {
        inputSlot2.setItemSilently(0, item)
    }

    private fun getOutputSlot(): ItemStack? {
        return outputSlot.getItem(0)
    }

    private fun setOutputSlot(item: ItemStack?) {
        outputSlot.setItemSilently(0, item)
    }

    /**
     * 负责渲染合并后的物品在 [MergingMenu.outputSlot] 里面的样子.
     */
    private fun renderOutputSlot(result: MergingSession.ReforgeResult): ItemStack {
        val clickToMerge = "<gray>[<aqua>点击确认合并</aqua>]".mini

        if (result.isSuccess) {
            // 渲染成功的结果

            // 渲染输出的物品
            val outputItemStack = result.output.apply {
                val renderingContext = MergingTableContext.MergeOutputSlot(session)
                ItemRenderers.MERGING_TABLE.render(this, renderingContext)
            }.itemStack

            return outputItemStack.edit {
                itemName = "<white>结果: <green>就绪".mini
                lore = lore.orEmpty() + buildList {
                    add(empty())
                    addAll(result.reforgeType.description)
                    addAll(result.reforgeCost.description)
                    add(empty())
                    add(clickToMerge)
                }.removeItalic
            }
        } else {
            // 渲染失败的结果

            return ItemStack.of(Material.BARRIER).edit {
                itemName = "<white>结果: <red>失败".mini
                lore = listOf(result.description).removeItalic
            }
        }
    }
}