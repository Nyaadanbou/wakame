package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.mod.*
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
import xyz.xenondevs.invui.item.*
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.properties.Delegates

/**
 * 定制台的主菜单, 也是玩家打开定制台后的代码入口.
 *
 * ## 用户流程
 * 按照整体设计 (假设每一步都没有意外发生):
 * - 玩家首先要将需要定制的物品放入 [inputSlot]
 * - 然后主菜单中便会出现若干个*子菜单*, 每个*子菜单*对应物品上的一个词条栏
 * - 在每一个子菜单中:
 *    - 玩家可以看到子菜单所关联的词条栏信息
 *    - 玩家可以将一个便携式核心放入子菜单的*输入容器*中
 *    - 这相当于告诉定制台: 我要消耗这个物品来定制这个词条栏
 * - 主菜单的 [outputSlot] 会实时显示定制之后的物品
 * - 玩家可以随时将定制后的物品从 [outputSlot] 中取出
 * - 如果玩家取出了 [outputSlot] 中的物品, 则相当于完成定制, 同时会消耗掉所有的输入容器中的物品
 *
 * ## 实现原则
 * - GUI 里面玩家能看到的所有物品必须全部都是深度克隆
 * - 所有被实际操作的物品均储存在 [ModdingSession]
 */
internal class ModdingMenu(
    val table: ModdingTable,
    val viewer: Player,
) : KoinComponent {
    companion object {
        private val MSG_CANCELLED = text { content("猫咪不可以!"); color(NamedTextColor.RED) }
    }

    /**
     * 向玩家展示定制台菜单.
     */
    fun open() {
        primaryWindow.open()
    }

    /**
     * 基于当前的所有状态执行一次定制.
     */
    fun executeReforge() {
        session.executeReforge()
    }

    /**
     * 基于 [session] 的当前状态更新 [outputSlot], 也就是定制后的物品.
     */
    fun updateOutputSlot() {
        val result = session.latestResult
        val output = renderOutputSlotForPreview(result)
        setOutputSlot(output)
    }

    /**
     * 基于 [session] 的当前状态更新子菜单, 也就是每个词条栏的定制菜单.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun updateReplaceGuis() {
        val replaceParams = session.replaceParams
        val replaceGuis = replaceParams.map { (_, replace) -> ReplaceMenu(this, replace) }
        setReplaceGuis(replaceGuis)
    }

    /**
     * 本菜单的 [ModdingSession].
     */
    val session: ModdingSession = SimpleModdingSession(table, viewer)

    /**
     * 玩家是否已经确认取出定制后的物品.
     *
     * 这只是个标记, 具体的作用取决于实现.
     */
    var confirmed: Boolean by Delegates.observable(false) { _, old, new ->
        logger.info("Confirmed status updated: $old -> $new")
    }

    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.MOD)

    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        guiPriority = 10
        setPreUpdateHandler(::onInputInventoryPreUpdate)
    }

    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1)).apply {
        guiPriority = 10
        setPreUpdateHandler(::onOutputInventoryPreUpdate)
    }

    private val primaryGui: ScrollGui<Gui> = ScrollGui.guis { builder ->
        builder.setStructure(
            ". . . x x x . . .",
            ". . . x x x . . .",
            ". i . x x x . o .",
            ". . . x x x . . .",
            ". . . x x x . . .",
            "# # # < # > # # #"
        )
        builder.addIngredient('.', SimpleItem(ItemStack(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('#', SimpleItem(ItemStack(Material.GREEN_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('i', inputSlot)
        builder.addIngredient('o', outputSlot, ItemWrapper(ItemStack(Material.BARRIER).hideTooltip(true)))
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }

    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(table.title.translateBy(viewer))
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    /**
     * 当 [inputSlot] 中的物品发生*变化前*调用.
     */
    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Input item updating: ${prevItem?.type} -> ${newItem?.type}")

        if (session.frozen) {
            logger.error("Modding session is frozen, but the player is trying to interact with the primary input slot. This is a bug!")
            event.isCancelled = true
            return
        }

        when {
            // 玩家尝试交换 inputSlot 中的物品:
            event.isSwap -> {
                viewer.sendMessage(MSG_CANCELLED)
                event.isCancelled = true
            }

            // 玩家尝试把物品放入 inputSlot:
            // 玩家将物品放入*inputSlot*, 意味着一个新的定制过程开始了.
            // 这里需要做的就是刷新 ModdingSession 的状态, 然后更新菜单.
            event.isAdd -> {
                session.inputItem = newItem
                updateReplaceGuis()
                updateOutputSlot()

                // 重置确认状态
                confirmed = false
            }

            // 玩家尝试把物品从 inputSlot 取出:
            // 玩家将物品从 inputSlot 取出, 意味着定制过程被 *中途* 终止了.
            // 我们需要把玩家放入定制台的所有物品 *原封不动* 的归还给玩家.
            event.isRemove -> {
                event.isCancelled = true

                // 归还玩家放入定制台的所有物品
                viewer.inventory.addItem(*session.getAllInputs())

                // 重置会话状态
                session.reset()

                // 清空菜单内容
                setInputSlot(null)
                setOutputSlot(null)
                setReplaceGuis(null)

                // 重置确认状态
                confirmed = false
            }
        }
    }

    /**
     * 当 [outputSlot] 中的物品发生*变化前*调用.
     */
    private fun onOutputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Output item updating: ${prevItem?.type} -> ${newItem?.type}")

        if (session.frozen) {
            logger.error("Modding session is frozen, but the player is trying to interact with the primary output slot. This is a bug!")
            event.isCancelled = true
            return
        }

        when {
            // 玩家向 outputSlot 中添加物品:
            event.isAdd || event.isSwap -> {
                viewer.sendMessage(MSG_CANCELLED)
                event.isCancelled = true
            }

            // 玩家从 outputSlot 中取出物品:
            event.isRemove -> {
                event.isCancelled = true

                // 如果玩家尝试取出定制后的物品, 意味着玩家想要完成这次定制.
                // 下面要做的就是检查玩家是否满足完成定制的所有要求.
                // - 如果满足则消耗资源, 给予定制后的物品.
                // - 如果不满足则终止操作, 展示玩家相应的提示.

                // 获取当前定制的结果
                val reforgeResult = session.latestResult

                // 如果结果成功的话:
                if (reforgeResult.successful) {

                    // 玩家必须先确认才能完成定制
                    if (!confirmed) {
                        val confirmIcon = renderOutputSlotForConfirm(reforgeResult)
                        setOutputSlot(confirmIcon)
                        confirmed = true
                        return
                    }

                    // 从玩家身上拿走需要的资源
                    reforgeResult.cost.take(viewer)

                    // 把所有输出的物品给予玩家
                    viewer.inventory.addItem(*session.getFinalOutputs())

                    // 重置会话状态
                    session.reset()

                    // 清空菜单内容
                    setInputSlot(null)
                    setOutputSlot(null)
                    setReplaceGuis(null)

                    // 重置确认状态
                    confirmed = false
                }

                // 如果结果失败的话:
                else {
                    viewer.sendMessage(MSG_CANCELLED)
                }
            }
        }
    }

    private fun onWindowClose() {
        // 将定制过程中玩家输入的所有物品归还给玩家
        viewer.inventory.addItem(*session.getAllInputs())

        // 冻结会话
        session.frozen = true
    }

    private fun onWindowOpen() {
        // NOP
    }

    @Suppress("SameParameterValue")
    private fun setInputSlot(stack: ItemStack?) {
        inputSlot.setItemSilently(0, stack)
    }

    private fun setOutputSlot(stack: ItemStack?) {
        outputSlot.setItemSilently(0, stack)
    }

    private fun setReplaceGuis(guis: List<Gui>?) {
        primaryGui.setContent(guis)
    }

    /**
     * 渲染定制结果在 [ModdingMenu.outputSlot] 里面的样子.
     * 本函数渲染的是 *确认图标*, 也就是提示玩家确认取出的图标.
     *
     * @param result 定制的结果
     * @return 渲染后的物品
     */
    private fun renderOutputSlotForConfirm(result: ModdingSession.ReforgeResult): ItemStack {
        val ret = ItemStack(Material.ANVIL)
        ret.editMeta { meta ->
            val name = "<white>再次点击取出".mini
            meta.itemName(name)
        }

        return ret
    }

    /**
     * 渲染定制结果在 [ModdingMenu.outputSlot] 里面的样子.
     * 本函数渲染的是 *普通图标*, 也就是物品经过定制之后的样子.
     *
     * @param result 定制的结果
     * @return 渲染后的物品
     */
    private fun renderOutputSlotForPreview(result: ModdingSession.ReforgeResult): ItemStack {
        val item = result.outputItem
        val ret: ItemStack

        if (result.successful) {
            // 定制成功了

            if (item == null) {
                ret = ItemStack(Material.BARRIER)
                ret.editMeta { meta ->
                    val name = "<white>结果: <red>内部错误".mini
                    meta.itemName(name)
                }
            } else {
                // FIXME 移除萌芽标签 / 实现 NekoStack#gui
                ret = item.itemStack
                ret.editMeta { meta ->
                    val name = "<white>结果: <green>就绪".mini
                    val lore = buildList {
                        addAll(result.description)
                    }

                    meta.itemName(name)
                    meta.lore(lore.removeItalic)
                }
            }
        } else {
            // 定制失败了

            ret = ItemStack(Material.BARRIER)
            ret.editMeta { meta ->
                val name = "<white>结果: <red>失败".mini
                val lore = buildList {
                    addAll(result.description)
                }

                meta.itemName(name)
                meta.lore(lore.removeItalic)
            }
        }

        return ret
    }

    /**
     * 向前翻页的 [Item].
     */
    private class PrevItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { it.itemName(text("上一页")) }
            return ItemWrapper(stack)
        }
    }

    /**
     * 向后翻页的 [Item].
     */
    private class NextItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            val stack = ItemStack(Material.PAPER)
            stack.editMeta { it.itemName(text("下一页")) }
            return ItemWrapper(stack)
        }
    }
}