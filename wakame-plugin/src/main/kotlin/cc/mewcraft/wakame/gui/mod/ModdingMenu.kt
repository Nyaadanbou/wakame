package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.reforge.mod.ModdingTable
import cc.mewcraft.wakame.util.hideTooltip
import cc.mewcraft.wakame.util.translateBy
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
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
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
        private const val PREFIX = ReforgeLoggerPrefix.MOD
    }

    /**
     * 向玩家展示定制台菜单.
     */
    fun open() {
        primaryWindow.open()
    }

    /**
     * 基于当前的所有状态更新 [outputSlot].
     *
     * 也就是定制后的物品的图标.
     */
    fun updateOutput() {
        val result = moddingSession.latestResult
        val output = ResultRender.normal(result)
        setOutputSlot(output)
    }

    /**
     * 基于当前的所有状态更新子菜单 (调用 [ScrollGui.setContent]).
     *
     * 也就是每个词条栏的定制菜单.
     */
    fun updateReplace() {
        val replaceMap = moddingSession.replaceParams
        val replaceGuis = replaceMap
            .map { (_, replace) -> ReplaceMenu(this, replace) }
            .map { it.gui }

        setReplaceGuis(replaceGuis)
    }

    /**
     * 本菜单的 [ModdingSession].
     */
    val moddingSession: ModdingSession = ModdingSessionFactory.create(
        this, null
    )

    /**
     * 玩家是否已经确认取出定制后的物品.
     *
     * 这只是个标记, 具体的作用取决于实现.
     */
    var confirmed: Boolean by Delegates.observable(false) { _, old, new ->
        logger.info("$PREFIX Confirmed status updated: $old -> $new")
    }

    private val logger: Logger by inject()

    private val inputSlot: VirtualInventory = VirtualInventory(intArrayOf(1))

    private val outputSlot: VirtualInventory = VirtualInventory(intArrayOf(1))

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
        logger.info("$PREFIX Input item updating: ${prevItem?.type} -> ${newItem?.type}")

        if (moddingSession.frozen) {
            logger.error("$PREFIX Modding session is frozen, but the player is trying to interact with the primary input slot. This is a bug!")
            event.isCancelled = true; return
        }

        when {
            // 玩家尝试交换 inputSlot 中的物品:
            event.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家尝试把物品放入 inputSlot:
            event.isAdd -> {
                // 玩家将物品放入*inputSlot*, 意味着一个新的定制过程开始了.
                // 这里需要做的就是刷新 ModdingSession 的状态, 然后更新菜单.

                val stack = newItem?.tryNekoStack ?: run {
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    event.isCancelled = true; return
                }

                moddingSession.sourceItem = stack
                updateReplace()
                updateOutput()

                // 重置确认状态
                confirmed = false
            }

            // 玩家尝试把物品从 inputSlot 取出:
            event.isRemove -> {
                // 玩家将物品从*inputSlot*取出, 意味着定制过程被*中途*终止了.
                // 我们需要把玩家放入定制台的所有物品*原封不动*的归还给玩家.

                event.isCancelled = true

                // 归还玩家放入定制台的所有物品
                val itemsToReturn = moddingSession.getAllPlayerInputs()
                viewer.inventory.addItem(*itemsToReturn.toTypedArray())

                // 重置会话状态
                moddingSession.reset()

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
        logger.info("$PREFIX Output item updating: ${prevItem?.type} -> ${newItem?.type}")

        if (moddingSession.frozen) {
            logger.error("$PREFIX Modding session is frozen, but the player is trying to interact with the primary output slot. This is a bug!")
            event.isCancelled = true; return
        }

        when {
            // 玩家向 outputSlot 中添加物品:
            event.isAdd || event.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家从 outputSlot 中取出物品:
            event.isRemove -> {
                event.isCancelled = true

                // 如果玩家尝试取出定制后的物品, 意味着玩家想要完成这次定制.
                // 下面要做的就是检查玩家是否满足完成定制的所有要求.
                // 如果满足就消耗资源, 给予定制后的物品.
                // 如果不满足则终止操作, 给玩家合适的提示.

                // 获取当前定制的结果
                val result = moddingSession.latestResult

                // 如果结果成功的话:
                if (result.successful) {

                    // 玩家必须先确认才能完成定制
                    if (!confirmed) {
                        val rendered = ResultRender.confirm(result)
                        setOutputSlot(rendered)
                        confirmed = true
                        return
                    }

                    // 储存所有需要给予玩家的物品
                    val itemsToGive = buildList {
                        // 定制后的物品
                        add(result.outputItem?.itemStack ?: run {
                            logger.error("$PREFIX Output item is null, but the player is trying to take it. This is a bug!")
                            return
                        })

                        // 未使用的物品, 一般是无效的耗材
                        addAll(moddingSession.getInapplicablePlayerInputs())
                    }
                    // 把所有输出的物品给予玩家
                    viewer.inventory.addItem(*itemsToGive.toTypedArray())

                    // 从玩家身上拿走需要的资源
                    result.cost.take(viewer)

                    // 重置会话状态
                    moddingSession.reset()

                    // 清空菜单内容
                    setInputSlot(null)
                    setOutputSlot(null)
                    setReplaceGuis(null)

                    // 重置确认状态
                    confirmed = false

                    return
                }

                // 如果结果失败的话:
                else {
                    viewer.sendPlainMessage("猫咪不可以!")
                }
            }
        }
    }

    private fun onWindowClose() {
        // 将定制过程中玩家输入的所有物品归还给玩家
        val itemsToReturn = moddingSession.getAllPlayerInputs()
        viewer.inventory.addItem(*itemsToReturn.toTypedArray())

        // 冻结会话
        moddingSession.frozen = true
    }

    private fun onWindowOpen() {
        // NOP
    }

    init {
        inputSlot.setPreUpdateHandler(::onInputInventoryPreUpdate)
        outputSlot.setPreUpdateHandler(::onOutputInventoryPreUpdate)
        inputSlot.guiPriority = 10
        outputSlot.guiPriority = 0
    }

    private fun setReplaceGuis(guis: List<Gui>?) {
        primaryGui.setContent(guis)
    }

    private fun setInputSlot(stack: ItemStack?) {
        inputSlot.setItemSilently(0, stack)
    }

    private fun setOutputSlot(stack: ItemStack?) {
        outputSlot.setItemSilently(0, stack)
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