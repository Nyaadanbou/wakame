package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.rerolling_table.RerollingTableContext
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import cc.mewcraft.wakame.item.reforgeHistory
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.reforge.reroll.RerollingTable
import cc.mewcraft.wakame.reforge.reroll.SimpleRerollingSession
import cc.mewcraft.wakame.util.itemLoreOrEmpty
import cc.mewcraft.wakame.util.itemNameOrType
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.ScrollGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.item.ItemProvider
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
) : Listener {

    /**
     * 给玩家显示 GUI.
     */
    fun open() {
        primaryWindow.open()
        viewer.sendMessage(TranslatableMessages.MSG_OPENED_REROLLING_MENU)
    }

    /**
     * 本菜单的 [RerollingSession].
     */
    val session: RerollingSession = SimpleRerollingSession(table, viewer)

    /**
     * 本菜单的日志记录器, 自带前缀.
     */
    // val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.REROLL)

    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        setPreUpdateHandler(::onInputInventoryPreUpdate)
    }
    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        setPreUpdateHandler(::onOutputInventoryPreUpdate)
    }
    private val primaryGui: ScrollGui<Gui> = ScrollGui.guis { builder ->
        builder.setStructure(*table.primaryMenuSettings.structure)
        builder.addIngredient('.', table.primaryMenuSettings.getSlotDisplay("background").resolveToItemWrapper())
        builder.addIngredient('i', inputSlot)
        builder.addIngredient('o', outputSlot, table.primaryMenuSettings.getSlotDisplay("output_empty").resolveToItemWrapper())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_VERTICAL)
    }
    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(table.primaryMenuSettings.title)
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    /**
     * 玩家是否已经确认取出重造后的物品.
     * 这只是个标记, 具体的作用取决于实现.
     */
    var confirmed: Boolean by Delegates.observable(false) { _, old, new ->
        // logger.info("Confirmed status updated: $old -> $new")
    }

    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    init {
        executeReforge() // 初始化时执行一次空的操作
        updateOutputSlot()
    }

    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        // logger.info("Input slot updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 inputSlot 里面的物品:
            event.isSwap -> {
                viewer.sendMessage(TranslatableMessages.MSG_ERR_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放进 inputSlot:
            // ... 说明玩家想要开始一次重造流程
            event.isAdd -> {
                // 设置本会话的源物品
                // 赋值完毕之后, session 内部的其他状态应该也要更新
                session.originalInput = newItem

                // 重新渲染放入的物品
                event.newItem = renderInputItem()

                updateSelectionGuis()
                updateOutputSlot()
            }

            // 玩家尝试从 inputSlot 拿出物品:
            // ... 说明玩家想中途放弃这次重造
            event.isRemove -> {
                event.isCancelled = true

                // 归还玩家输入的所有物品
                viewer.inventory.addItem(*session.getAllInputs())

                // 重置会话的状态
                session.reset()
                confirmed = false

                updateInputSlot()
                updateSelectionGuis()
                updateOutputSlot()
            }
        }
    }

    private fun onOutputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        // logger.info("Output item updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 outputSlot 里面的物品:
            event.isSwap -> {
                viewer.sendMessage(TranslatableMessages.MSG_ERR_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放进 outputSlot:
            event.isAdd -> {
                viewer.sendMessage(TranslatableMessages.MSG_ERR_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试从 outputSlot 拿出物品:
            // ... 说明玩家想要完成这次重造
            event.isRemove -> {
                event.isCancelled = true

                // 首先获得当前的重造结果
                val result = session.latestResult
                if (!result.isSuccess) {
                    return
                }

                // 再次更新输出容器
                // 因为此时玩家所持有的资源可能发生了变化 (例如突然收到了一笔金币)
                if (!confirmed) {
                    confirmed = true
                    updateOutputSlot()
                    return
                }

                // 判断玩家是否有足够的资源
                if (!result.reforgeCost.test(viewer)) {
                    setOutputSlot(table.primaryMenuSettings.getSlotDisplay("output_insufficient_resource").resolveToItemStack())
                    return
                }

                // 从玩家扣除所需的资源
                result.reforgeCost.take(viewer)

                // 把重造后的物品给玩家
                viewer.inventory.addItem(*session.getFinalOutputs())

                // 重置会话状态
                session.reset()
                confirmed = false

                updateInputSlot()
                updateSelectionGuis()
                updateOutputSlot()
            }
        }
    }

    /**
     * 基于 [session] 的当前状态执行一次重造.
     */
    fun executeReforge() {
        session.executeReforge()
    }

    /**
     * 基于 [session] 的当前状态刷新输入容器.
     *
     * 该函数不应该用于在事件 [ItemPreUpdateEvent] 中重新渲染玩家放入的物品.
     * 要重新渲染玩家放入的物品, 应该在事件中使用 [renderInputItem] 的返回值.
     *
     * 具体代码如下:
     * ```kotlin
     * event.newItem = renderInputItem()
     * ```
     */
    fun updateInputSlot() {
        setInputSlot(renderInputItem())
    }

    /**
     * 基于 [session] 的当前状态刷新输出容器.
     */
    fun updateOutputSlot() {
        val reforgeResult = session.latestResult

        val newItemStack = if (reforgeResult.isSuccess) {
            // 如果可以重造:

            // 这里修改输入的[原始物品], 作为[输出物品]的预览.
            // 我们重新渲染[原始物品]上要修改的核心, 这样可以准确的反映哪些部分被修改了.
            // 我们不选择渲染*重造之后*的物品, 因为那样必须绕很多弯路, 非常不好实现.

            // 输入的[原始物品]
            val previewItem = session.originalInput?.shadowNeko(true) ?: error("result is successful but the input item is null - this is a bug!")

            // 单独把 ReforgeHistory 赋值给[预览物品],
            // 这样玩家就可以知道物品重铸后的次数是多少.
            previewItem.reforgeHistory = reforgeResult.output.reforgeHistory

            // 使用重造台的物品渲染器渲染[预览物品]
            ItemRenderers.REROLLING_TABLE.render(previewItem, RerollingTableContext(session, RerollingTableContext.Slot.OUTPUT))

            val slotDisplayId = if (confirmed) "output_ok_confirmed" else "output_ok_unconfirmed"
            val slotDisplayResolved = table.primaryMenuSettings.getSlotDisplay(slotDisplayId).resolveEverything {
                standard { component("item_name", previewItem.wrapped.itemNameOrType) }
                folded("item_lore", previewItem.wrapped.itemLoreOrEmpty)
                folded("cost_description", reforgeResult.reforgeCost.description)
            }

            slotDisplayResolved.applyTo(previewItem.wrapped)
        } else {
            // 如果不可重造:

            // 由于根本无法重造, 所以也不存在重造后的物品.
            // 这里直接使用一个新的物品堆叠来展示失败的结果.
            table.primaryMenuSettings.getSlotDisplay("output_failure").resolveToItemStack {
                folded("failure_reason", reforgeResult.description)
            }
        }

        setOutputSlot(newItemStack)
    }

    /**
     * 基于 [session] 的当前状态刷新所有的 [SelectionMenu].
     */
    fun updateSelectionGuis() {
        val guis = session.selectionMap.values
            // 只为可被重造的核孔创建菜单
            .filter { selection -> selection.changeable }
            // 给每个 Selection 创建菜单
            .map { selection -> SelectionMenu(this, selection) }

        setSelectionGuis(guis)
    }

    /**
     * 用于重新渲染玩家输入的源物品.
     */
    private fun renderInputItem(): ItemStack? {
        val sourceItem = session.usableInput
            ?: return session.originalInput // sourceItem 为 null 时, 说明这个物品没法定制, 直接返回玩家放入的原物品

        val renderingCtx = RerollingTableContext(session, RerollingTableContext.Slot.INPUT)
        ItemRenderers.REROLLING_TABLE.render(sourceItem, renderingCtx)
        val newItemStack = sourceItem.itemStack

        return newItemStack
    }

    private fun onWindowOpen() {
        playerInventorySuppressor.startListening()

        // logger.info("Rerolling window opened for ${viewer.name}")
    }

    private fun onWindowClose() {
        playerInventorySuppressor.stopListening()

        // logger.info("Rerolling window closed for ${viewer.name}")

        viewer.inventory.addItem(*session.getAllInputs())

        session.reset()
        session.frozen = true
    }

    private fun setInputSlot(stack: ItemStack?) {
        inputSlot.setItemSilently(0, stack)
    }

    private fun setOutputSlot(stack: ItemStack?) {
        outputSlot.setItemSilently(0, stack)
    }

    private fun setSelectionGuis(guis: List<Gui>?) {
        primaryGui.setContent(guis)
    }

    private inner class PrevItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            return table.primaryMenuSettings.getSlotDisplay("prev_page").resolveToItemWrapper()
        }
    }

    private inner class NextItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            return table.primaryMenuSettings.getSlotDisplay("next_page").resolveToItemWrapper()
        }
    }
}