package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.rerolling_table.RerollingTableContext
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.item.unsafeEdit
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.reroll.RerollingSession
import cc.mewcraft.wakame.reforge.reroll.RerollingTable
import cc.mewcraft.wakame.reforge.reroll.SimpleRerollingSession
import cc.mewcraft.wakame.util.decorate
import cc.mewcraft.wakame.util.edit
import cc.mewcraft.wakame.util.removeItalic
import me.lucko.helper.text3.mini
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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
) : Listener, KoinComponent {

    /**
     * 给玩家显示 GUI.
     */
    fun open() {
        primaryWindow.open()
        viewer.sendMessage(MessageConstants.MSG_OPENED_REROLLING_MENU)
    }

    /**
     * 本菜单的 [RerollingSession].
     */
    val session: RerollingSession = SimpleRerollingSession(table, viewer)

    /**
     * 本菜单的日志记录器, 自带前缀.
     */
    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.REROLL)

    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        setPreUpdateHandler(::onInputInventoryPreUpdate)
    }
    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        setPreUpdateHandler(::onOutputInventoryPreUpdate)
    }
    private val primaryGui: ScrollGui<Gui> = ScrollGui.guis { builder ->
        builder.setStructure(
            ". . . . . . . . .",
            ". . x x x x x . .",
            ". < x x x x x > .",
            ". . . . . . . . .",
            ". . i . . . o . .",
            ". . . . . . . . .",
        )
        builder.addIngredient('.', SimpleItem(ItemStack(Material.BLACK_STAINED_GLASS_PANE).edit { hideTooltip = true }))
        builder.addIngredient('i', inputSlot)
        builder.addIngredient('o', outputSlot, ItemWrapper(ItemStack(Material.BARRIER).edit { hideTooltip = true }))
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

    /**
     * 玩家是否已经确认取出重造后的物品.
     * 这只是个标记, 具体的作用取决于实现.
     */
    var confirmed: Boolean by Delegates.observable(false) { _, old, new ->
        logger.info("Confirmed status updated: $old -> $new")
    }

    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Input slot updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 inputSlot 里面的物品:
            event.isSwap -> {
                viewer.sendMessage(MessageConstants.MSG_ERR_CANCELLED)
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
        logger.info("Output item updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 outputSlot 里面的物品:
            event.isSwap -> {
                viewer.sendMessage(MessageConstants.MSG_ERR_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放进 outputSlot:
            event.isAdd -> {
                viewer.sendMessage(MessageConstants.MSG_ERR_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试从 outputSlot 拿出物品:
            // ... 说明玩家想要完成这次重造
            event.isRemove -> {
                event.isCancelled = true

                if (session.usableInput == null) {
                    logger.error("An item is being removed from the output slot, but the source item is null. This is a bug!")
                    return
                }

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
                    setOutputSlot(ItemStack.of(Material.BARRIER).edit {
                        itemName = text {
                            content("结果: ").color(NamedTextColor.WHITE)
                            append(text { content("资源不足").color(NamedTextColor.RED) })
                        }
                    })
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

            // 这里修改输入的原始物品, 作为输出物品的预览.
            // 我们重新渲染*原始物品*上要修改的核心, 这样可以准确的反映哪些部分被修改了.
            // 我们不选择渲染*重造之后*的物品, 因为那样必须绕很多弯路, 非常不好实现.

            val previewItem = session.originalInput?.shadowNeko(true) ?: error("Result is successful but the input item is null. This is a bug!")
            ItemRenderers.REROLLING_TABLE.render(previewItem, RerollingTableContext(session, RerollingTableContext.Slot.OUTPUT))
            previewItem.unsafeEdit {
                lore = lore.orEmpty() + buildList {
                    add(empty())
                    addAll(reforgeResult.reforgeCost.description)

                    if (confirmed) {
                        // 如果玩家已经确认, 则加上确认提示
                        add(empty())
                        add("<gray>[<aqua>点击确认重造</aqua>]".mini)
                    }
                }.removeItalic
            }

        } else {
            // 如果不可重造:

            // 由于根本无法重造, 所以也不存在重造后的物品.
            // 这里直接创建一个新的物品堆叠 (暂时用屏障).
            ItemStack.of(Material.BARRIER).edit {
                itemName = "<white>结果: <red>失败".mini
                lore = reforgeResult.description.removeItalic
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

        val context = RerollingTableContext(session, RerollingTableContext.Slot.INPUT)
        ItemRenderers.REROLLING_TABLE.render(sourceItem, context)
        val newItemStack = sourceItem.itemStack

        return newItemStack
    }

    private fun onWindowOpen() {
        playerInventorySuppressor.startListening()

        logger.info("Rerolling window opened for ${viewer.name}")
    }

    private fun onWindowClose() {
        playerInventorySuppressor.stopListening()

        logger.info("Rerolling window closed for ${viewer.name}")

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