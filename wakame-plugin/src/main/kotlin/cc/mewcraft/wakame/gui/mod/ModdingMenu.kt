package cc.mewcraft.wakame.gui.mod

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.display2.ItemRenderers
import cc.mewcraft.wakame.display2.implementation.modding_table.ModdingTableContext
import cc.mewcraft.wakame.gui.common.PlayerInventorySuppressor
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.mod.ModdingSession
import cc.mewcraft.wakame.reforge.mod.ModdingTable
import cc.mewcraft.wakame.reforge.mod.SimpleModdingSession
import cc.mewcraft.wakame.util.decorate
import cc.mewcraft.wakame.util.itemLoreOrEmpty
import cc.mewcraft.wakame.util.itemNameOrType
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
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.controlitem.ScrollItem
import xyz.xenondevs.invui.window.Window
import xyz.xenondevs.invui.window.type.context.setTitle
import kotlin.properties.Delegates

/**
 * 定制台的主菜单, 也是玩家打开定制台后的代码入口.
 *
 * ## 用户流程
 * 按照整体设计 (假设每一步都没有意外发生):
 * - 玩家首先要将需要定制的物品放入 [inputSlot]
 * - 然后主菜单中便会出现若干个*子菜单*, 每个*子菜单*对应物品上的一个核孔
 * - 在每一个子菜单中:
 *    - 玩家可以看到子菜单所关联的核孔信息
 *    - 玩家可以将一个便携式核心放入子菜单的*输入容器*中
 *    - 这相当于告诉定制台: 我要消耗这个物品来定制这个核孔
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

    /**
     * 向玩家展示定制台菜单.
     */
    fun open() {
        primaryWindow.open()
        viewer.sendMessage(MessageConstants.MSG_OPENED_MODDING_MENU)
    }

    /**
     * 本菜单的 [ModdingSession].
     */
    val session: ModdingSession = SimpleModdingSession(table, viewer)

    /**
     * 玩家是否已经确认取出定制后的物品.
     * 这只是个标记, 具体的作用取决于实现.
     */
    var confirmed: Boolean by Delegates.observable(false) { _, old, new ->
        // logger.info("Confirmed status updated: $old -> $new")
    }

    /**
     * 本菜单的日志记录器, 自带前缀.
     */
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
        builder.setStructure(*table.primaryMenuSettings.structure)
        builder.addIngredient('.', table.primaryMenuSettings.getSlotDisplay("background").resolveToItemWrapper())
        builder.addIngredient('#', table.primaryMenuSettings.getSlotDisplay("background2").resolveToItemWrapper())
        builder.addIngredient('i', inputSlot)
        builder.addIngredient('o', outputSlot, table.primaryMenuSettings.getSlotDisplay("output_slot_empty").resolveToItemWrapper())
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }

    private val selectionMenus: MutableList<ReplaceMenu> = mutableListOf()

    private val primaryWindow: Window = Window.single { builder ->
        builder.setGui(primaryGui)
        builder.setTitle(table.primaryMenuSettings.title)
        builder.setViewer(viewer)
        builder.addOpenHandler(::onWindowOpen)
        builder.addCloseHandler(::onWindowClose)
    }

    private val playerInventorySuppressor = PlayerInventorySuppressor(viewer)

    init {
        executeReforge() // 初始化时执行一次空的操作
        updateOutputSlot()
    }

    /**
     * 当 [inputSlot] 中的物品发生*变化前*调用.
     */
    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        // logger.info("Input item updating: ${prevItem?.type} -> ${newItem?.type}")

        if (session.frozen) {
            event.isCancelled = true
            logger.error("Modding session is frozen, but the player is trying to interact with the primary input slot. This is a bug!")
            return
        }

        when {
            // 玩家尝试交换 inputSlot 中的物品:
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendMessage(MessageConstants.MSG_ERR_CANCELLED)
            }

            // 玩家尝试把物品放入 inputSlot:
            // 玩家将物品放入*inputSlot*, 意味着一个新的定制过程开始了.
            // 这里需要做的就是刷新 session 的状态, 然后更新菜单的内容.
            event.isAdd -> {
                session.originalInput = newItem

                // 重新渲染放入的物品
                event.newItem = renderInputItem()

                confirmed = false
                recreateReplaceGuis()
                updateOutputSlot()
            }

            // 玩家尝试把物品从 inputSlot 取出:
            // 玩家将物品从 inputSlot 取出, 意味着定制过程被 *中途* 终止了.
            // 我们需要把玩家放入定制台的所有物品 *原封不动* 的归还给玩家.
            event.isRemove -> {
                event.isCancelled = true

                // 归还玩家放入定制台的所有物品
                viewer.inventory.addItem(*session.getAllInputs())

                session.reset()
                confirmed = false
                updateInputSlot()
                recreateReplaceGuis()
                updateOutputSlot()
            }
        }
    }

    /**
     * 当 [outputSlot] 中的物品发生*变化前*调用.
     */
    private fun onOutputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        // logger.info("Output item updating: ${prevItem?.type} -> ${newItem?.type}")

        if (session.frozen) {
            logger.error("Modding session is frozen, but the player is trying to interact with the primary output slot. This is a bug!")
            event.isCancelled = true
            return
        }

        when {
            // 玩家向 outputSlot 中添加物品:
            event.isAdd || event.isSwap -> {
                event.isCancelled = true
                viewer.sendMessage(MessageConstants.MSG_ERR_CANCELLED)
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
                if (reforgeResult.isSuccess) {

                    // 玩家必须先确认才能完成定制
                    if (!confirmed) {
                        confirmed = true
                        updateOutputSlot()
                        return
                    }

                    // 检查玩家是否有足够的资源完成定制
                    if (!reforgeResult.reforgeCost.test(viewer)) {
                        setOutputSlot(table.primaryMenuSettings.getSlotDisplay("output_slot_insufficient_resource").resolveToItemStack())
                        return
                    }

                    // 从玩家身上拿走本次定制需要的资源
                    reforgeResult.reforgeCost.take(viewer)

                    // 把会话中所有的物品输出给予玩家
                    viewer.inventory.addItem(*session.getFinalOutputs())

                    session.reset()
                    confirmed = false
                    updateInputSlot()
                    recreateReplaceGuis()
                    updateOutputSlot()
                }
            }
        }
    }

    private fun onWindowOpen() {
        playerInventorySuppressor.startListening()

        logger.info("Modding window opened for ${viewer.name}")
    }

    private fun onWindowClose() {
        playerInventorySuppressor.stopListening()

        logger.info("Modding window closed for ${viewer.name}")

        // 将定制过程中玩家输入的所有物品归还给玩家
        viewer.inventory.addItem(*session.getAllInputs())

        // 冻结会话
        session.reset()
        session.frozen = true
    }

    /**
     * 基于当前的所有状态执行一次定制.
     */
    fun executeReforge() {
        session.executeReforge()
    }

    /**
     * 基于 [session] 的当前状态更新 [inputSlot], 也就是玩家放入的物品.
     */
    fun updateInputSlot() {
        setInputSlot(renderInputItem())
    }

    /**
     * 基于 [session] 的当前状态更新 [outputSlot], 也就是定制后的物品.
     */
    fun updateOutputSlot() {
        val reforgeResult = session.latestResult
        val newItemStack = if (reforgeResult.isSuccess) {
            // 定制成功了:

            // 用定制台的渲染器重新渲染物品
            val outputNekoStack = reforgeResult.output ?: error("output item is null, but the result is successful. This is a bug!")
            val renderingContext = ModdingTableContext.Output(session)
            ItemRenderers.MODDING_TABLE.render(outputNekoStack, renderingContext)
            val outputItemStack = outputNekoStack.wrapped

            // 再用 SlotDisplay 处理一下
            table.primaryMenuSettings.getSlotDisplay("output_slot_ready").resolveToItemStack {
                standard { component("item_name", outputItemStack.itemNameOrType) }
                folded("item_lore", outputItemStack.itemLoreOrEmpty)
            }
        } else {
            // 定制失败了:

            table.primaryMenuSettings.getSlotDisplay("output_slot_failed").resolveToItemStack {
                folded("cost_description", reforgeResult.reforgeCost.description)
                folded("result_description", reforgeResult.description)
            }
        }

        setOutputSlot(newItemStack)
    }

    /**
     * 刷新当前所有的 [selectionMenus].
     */
    fun refreshReplaceGuis(excluded: ReplaceMenu) {
        selectionMenus.filter { it !== excluded }.forEach { menu ->
            // 让这个 replace 单独重新执行一次重铸, 更新状态.
            // 这样之后在主菜单执行重铸时, 才能读取最新的状态.
            menu.replace.bake()
            menu.updateInputSlot()
        }
    }

    /**
     * 基于 [session] 的当前状态更新子菜单, 为每个核孔创建好一个新的 [ReplaceMenu].
     */
    private fun recreateReplaceGuis() {
        val menus = session.replaceParams.values
            // 只为可被定制的核孔创建菜单
            .filter { replace -> replace.changeable }
            // 给每个 Replace 创建菜单
            .map { replace -> ReplaceMenu(this, replace) }

        selectionMenus.clear()
        selectionMenus += menus

        setReplaceGuis(menus.map(ReplaceMenu::primaryGui))
    }

    /**
     * 基于 [session] 的当前状态更新 [inputSlot], 也就是玩家放入的物品.
     */
    private fun renderInputItem(): ItemStack? {
        val sourceItem = session.usableInput
            ?: return session.originalInput // sourceItem 为 null 时, 说明这个物品没法定制, 直接返回玩家放入的原物品

        // 用定制台的渲染器重新渲染物品
        val renderingCtx = ModdingTableContext.Input(session)
        ItemRenderers.MODDING_TABLE.render(sourceItem, renderingCtx)
        val newItemStack = sourceItem.itemStack

        // 再用 SlotDisplay 处理一下
        table.primaryMenuSettings.getSlotDisplay("input_slot").resolveEverything {
            standard { component("item_name", newItemStack.itemNameOrType) }
            folded("item_lore", newItemStack.itemLoreOrEmpty)
        }.applyTo(newItemStack)

        return newItemStack
    }

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
     * 向前翻页的 [Item].
     */
    private inner class PrevItem : ScrollItem(-1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            return table.primaryMenuSettings.getSlotDisplay("prev_page").resolveToItemWrapper()
        }
    }

    /**
     * 向后翻页的 [Item].
     */
    private inner class NextItem : ScrollItem(1) {
        override fun getItemProvider(gui: ScrollGui<*>): ItemProvider {
            return table.primaryMenuSettings.getSlotDisplay("next_page").resolveToItemWrapper()
        }
    }
}