package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.rerolling_table.RerollingTableContext
import cc.mewcraft.wakame.gui.common.GuiMessages
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.reroll.*
import cc.mewcraft.wakame.util.*
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component.*
import org.bukkit.Material
import org.bukkit.entity.Player
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

/**
 * 重造台的主菜单, 也是玩家打开重造台后的代码入口.
 */
internal class RerollingMenu(
    val table: RerollingTable,
    val viewer: Player,
) : KoinComponent {

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
        val itemStack = renderOutputSlot()
        setOutputSlot(itemStack)
    }

    /**
     * 本菜单的 [RerollingSession].
     */
    val session: RerollingSession = SimpleRerollingSession(table, viewer)

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

    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Input slot updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            // 玩家尝试交换 inputSlot 里面的物品:
            event.isSwap -> {
                viewer.sendMessage(GuiMessages.MESSAGE_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放进 inputSlot:
            // ... 说明玩家想要开始一次重造流程
            event.isAdd -> {
                val added = newItem?.customNeko ?: run {
                    viewer.sendMessage(GuiMessages.MESSAGE_CANCELLED)
                    event.isCancelled = true
                    return
                }

                // 设置本会话的源物品
                session.inputItem = newItem

                // 重新渲染放入的物品
                event.newItem = renderInputSlot(added)

                // 创建并设置子菜单
                setSelectionMenus(createSelectionMenus())

                // 更新输出容器
                updateOutput()
            }

            // 玩家尝试从 inputSlot 拿出物品:
            // ... 说明玩家想中途放弃这次重造
            event.isRemove -> {
                event.isCancelled = true

                // 归还玩家输入的所有物品
                viewer.inventory.addItem(*session.getAllInputs())

                // 重置会话的状态
                session.reset()

                // 清空菜单内容
                setInputSlot(null)
                setOutputSlot(null)
                setSelectionMenus(null)
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
                viewer.sendMessage(GuiMessages.MESSAGE_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放进 outputSlot:
            event.isAdd -> {
                viewer.sendMessage(GuiMessages.MESSAGE_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试从 outputSlot 拿出物品:
            // ... 说明玩家想要完成这次重造
            event.isRemove -> {
                event.isCancelled = true

                if (session.sourceItem == null) {
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
                updateOutput()

                // 判断玩家是否有足够的资源
                if (!result.reforgeCost.test(viewer)) {
                    viewer.sendMessage(GuiMessages.MESSAGE_INSUFFICIENT_RESOURCES)
                    return
                }

                // 把重造后的源物品给玩家
                viewer.inventory.addItem(result.output.itemStack) // TODO getFinalOutputs

                // 重置会话状态
                session.reset()

                // 清空菜单内容
                setInputSlot(null)
                setOutputSlot(null)
                setSelectionMenus(null)
            }
        }
    }

    private fun onWindowClose() {
        logger.info("Rerolling window closed for ${viewer.name}")

        setInputSlot(null)
        setOutputSlot(null)
        setSelectionMenus(null)

        viewer.inventory.addItem(*session.getAllInputs())

        session.reset()
        session.frozen = true
    }

    private fun onWindowOpen() {
        logger.info("RerollingMenu opened for ${viewer.name}")
    }

    private fun createSelectionMenus(): List<Gui> {
        return session.selectionMap.map { (_, sel) -> SelectionMenu(this, sel) }
    }

    private fun setSelectionMenus(guis: List<Gui>?) {
        primaryGui.setContent(guis)
    }

    @Suppress("SameParameterValue")
    private fun setInputSlot(stack: ItemStack?) {
        inputSlot.setItemSilently(0, stack)
    }

    private fun setOutputSlot(stack: ItemStack?) {
        outputSlot.setItemSilently(0, stack)
    }

    /**
     * 渲染 [RerollingMenu.inputSlot] 里面的内容.
     */
    private fun renderInputSlot(source: NekoStack): ItemStack {
        val context = RerollingTableContext(RerollingTableContext.Slot.INPUT, session)
        ItemRenderers.REROLLING_TABLE.render(source, context)
        return source.itemStack
    }

    /**
     * 渲染 [RerollingMenu.outputSlot] 里面的内容.
     */
    private fun renderOutputSlot(): ItemStack {
        val result = session.latestResult

        if (result.isSuccess) {
            // 如果可以重造:

            // 这里修改输入的原始物品, 作为输出物品的预览.
            // 我们重新渲染*原始物品*上要修改的核心, 这样可以准确的反映哪些部分被修改了.
            // 我们不选择渲染*重造之后*的物品, 因为那样必须绕很多弯路, 非常不好实现.

            val previewItem = session.inputItem?.customNeko ?: error("Result is successful but the input item is null. This is a bug!")
            ItemRenderers.REROLLING_TABLE.render(previewItem, RerollingTableContext(RerollingTableContext.Slot.OUTPUT, session))

            return previewItem.directEdit {
                lore = lore.orEmpty() + buildList {
                    add(empty())
                    addAll(result.reforgeCost.description)
                    add(empty())
                    add("<gray>[<aqua>点击确认重造</aqua>]".mini)
                }.removeItalic
            }

        } else {
            // 如果不可重造:

            // 由于根本无法重造, 所以也不存在重造后的物品.
            // 这里直接创建一个新的物品堆叠 (屏障).
            return ItemStack.of(Material.BARRIER).edit {
                itemName = "<white>结果: <red>失败".mini
                lore = result.description.removeItalic
            }
        }
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