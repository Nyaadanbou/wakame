package cc.mewcraft.wakame.gui.reroll

import cc.mewcraft.wakame.item.isCustomNeko
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.reroll.*
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
        private val MESSAGE_CANCELLED = text { content("猫咪不可以!"); color(NamedTextColor.RED) }
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
        val output = renderOutputSlot(session)
        setOutputSlot(output)
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
                viewer.sendMessage(MESSAGE_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放进 inputSlot:
            // ... 说明玩家想要开始一次重造流程
            event.isAdd -> {
                val ns = newItem?.takeIf { it.isCustomNeko }?.tryNekoStack ?: run {
                    viewer.sendMessage(MESSAGE_CANCELLED)
                    event.isCancelled = true
                    return
                }

                // 给会话输入源物品
                session.sourceItem = ns

                // 创建并设置子菜单
                setSelectionGuis(createSelectionGuis())

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
                setSelectionGuis(null)
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
                viewer.sendMessage(MESSAGE_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放进 outputSlot:
            event.isAdd -> {
                viewer.sendMessage(MESSAGE_CANCELLED)
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
                    return
                }

                // 把重造后的源物品给玩家
                viewer.inventory.addItem(result.outputItem.itemStack) // TODO getFinalOutputs

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
        logger.info("Rerolling window closed for ${viewer.name}")

        setInputSlot(null)
        setOutputSlot(null)
        setSelectionGuis(null)

        viewer.inventory.addItem(*session.getAllInputs())

        session.reset()
        session.frozen = true
    }

    private fun onWindowOpen() {
        logger.info("RerollingMenu opened for ${viewer.name}")
    }

    private fun createSelectionGuis(): List<Gui> {
        return session.selectionMap.map { (_, sel) -> SelectionMenu(this, sel) }
    }

    private fun setSelectionGuis(guis: List<Gui>?) {
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
     * 负责渲染 [RerollingMenu.outputSlot] 里面的内容.
     */
    private fun renderOutputSlot(session: RerollingSession): ItemStack {
        // TODO 更丰富的结果预览:
        //  能够显示哪些词条栏会被重造, 哪些不会
        //  这要求对渲染模块进行重构 ...

        val viewer = session.viewer
        val result = session.latestResult
        val item = result.outputItem // deep clone

        if (result.isSuccess) {
            // 如果可以重造:

            if (!result.reforgeCost.test(viewer)) {
                // 如果玩家没有足够的资源:

                val ret = ItemStack(Material.BARRIER)
                ret.editMeta { meta ->
                    val name = "<white>结果: <red>资源不足".mini
                    val lore = buildList {
                        addAll(result.description)
                        addAll(result.reforgeCost.description)
                    }

                    meta.itemName(name)
                    meta.lore(lore.removeItalic)
                }

                return ret
            }

            // 移除物品的萌芽数据
            item.erase() // FIXME NekoStackDisplay

            // 在原物品的基础上做修改
            // 这样可以保留物品的类型以及其他的原版组件信息
            val ret = item.itemStack
            ret.editMeta { meta ->
                val name = "<white>结果: <green>就绪".mini
                val lore = buildList {
                    addAll(result.description)
                    addAll(result.reforgeCost.description)
                    add(empty())
                    add("<gray>点击确认重造".mini)
                }

                meta.displayName(name.removeItalic)
                meta.lore(lore.removeItalic)
            }

            return ret

        } else {
            // 如果不可重造:

            // 在新创建的物品上做修改
            // 因为根本无法重造所以原物品的信息就无所谓了
            val ret = ItemStack(Material.BARRIER)
            ret.editMeta { meta ->
                val name = "<white>结果: <red>失败".mini
                val lore = buildList {
                    addAll(result.description)
                }

                meta.itemName(name)
                meta.lore(lore.removeItalic)
            }

            return ret
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