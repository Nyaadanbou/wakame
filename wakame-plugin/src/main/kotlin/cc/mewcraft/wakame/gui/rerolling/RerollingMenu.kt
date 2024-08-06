package cc.mewcraft.wakame.gui.rerolling

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.reforge.rerolling.RerollingSession
import cc.mewcraft.wakame.reforge.rerolling.RerollingTable
import cc.mewcraft.wakame.reforge.rerolling.SimpleRerollingSession
import cc.mewcraft.wakame.util.hideTooltip
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextDecoration
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
import xyz.xenondevs.invui.inventory.event.ItemPostUpdateEvent
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
class RerollingMenu(
    val table: RerollingTable,
    val viewer: Player,
) : KoinComponent {

    fun open() {
        primaryWindow.open(viewer)
    }

    fun refreshOutput() {
        val session = rerollingSession ?: run {
            logger.info("Trying to refresh output without a session.")
            return
        }
        val result = session.reforge()
        val cost = result.cost // TODO 计算花费
        val copy = result.item
        val outputRenderer = RerollingOutputItemRenderer()
        outputRenderer.render(copy)
        fillOutputSlot(copy.handle)
    }

    private val logger: Logger by inject()

    private val inputInventory: VirtualInventory = VirtualInventory(intArrayOf(1))
    private val outputInventory: VirtualInventory = VirtualInventory(intArrayOf(1))
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
        builder.addIngredient('#', SimpleItem(ItemStack(Material.GREEN_STAINED_GLASS_PANE).hideTooltip(true)))
        builder.addIngredient('i', inputInventory)
        builder.addIngredient('o', outputInventory)
        builder.addIngredient('<', PrevItem())
        builder.addIngredient('>', NextItem())
        builder.addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
    }
    private val primaryWindow: Window.Builder.Normal.Single = Window.single().apply {
        setGui(primaryGui)
        setTitle(text("重造台").decorate(TextDecoration.BOLD))
    }

    private var _rerollingSession: RerollingSession? = null
    var rerollingSession: RerollingSession?
        get() = _rerollingSession
        set(value) {
            _rerollingSession = value
            if (value == null) {
                logger.info("Rerolling session cleared.")
            } else {
                logger.info("Rerolling session set: ${value.inputNekoStack.key}")
            }
        }

    init {
        inputInventory.setPreUpdateHandler(::onInputInventoryPreUpdate)
        inputInventory.setPostUpdateHandler(::onInputInventoryPostUpdate)
        outputInventory.setPreUpdateHandler(::onOutputInventoryPreUpdate)
        outputInventory.setPostUpdateHandler(::onOutputInventoryPostUpdate)
        primaryWindow.addCloseHandler(::onWindowClose)
        primaryWindow.addOpenHandler(::onWindowOpen)
    }

    private fun onInputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Input item updating: ${prevItem?.type} -> ${newItem?.type}")

        when {
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendPlainMessage("不支持交换物品.")
            }

            event.isAdd -> {
                // 不是 NekoStack - 返回
                val stack = newItem?.tryNekoStack ?: run {
                    event.isCancelled = true
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    return
                }

                // 不是有词条栏的物品 - 返回
                if (!stack.components.has(ItemComponentTypes.CELLS)) {
                    event.isCancelled = true
                    viewer.sendPlainMessage("请放入一个拥有词条栏的物品!")
                    return
                }

                // 创建会话
                val session = createRerollingSession(viewer, stack) ?: run {
                    event.isCancelled = true
                    viewer.sendPlainMessage("该物品不支持重造!")
                    return
                }

                // 设置会话
                rerollingSession = session

                // 将*克隆*放入输入容器
                event.newItem = stack.itemStack

                val selectionMenus = session.selectionSessions.map { (_, cellSession) ->
                    createSelectionMenu(this, cellSession)
                }
                val selectionGuis = selectionMenus.map {
                    it.primaryGui
                }
                fillSelectionGuis(selectionGuis)

                // 更新输出容器
                // 提示玩家可以从输出容器中取出物品
                refreshOutput()
            }

            event.isRemove -> {
                event.isCancelled = true

                val session = rerollingSession ?: run {
                    logger.error("Rerolling session (viewer: ${viewer.name}) is null, but input item is being removed. This is a bug!")
                    return
                }

                clearInputSlot()
                clearOutputSlot()
                clearSelectionGuis()

                session.returnInput(viewer)
                session.clearSelections()

                session.frozen = true
                rerollingSession = null
            }
        }
    }

    private fun onInputInventoryPostUpdate(event: ItemPostUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Input item updated: ${prevItem?.type} -> ${newItem?.type}")
    }

    private fun onOutputInventoryPreUpdate(event: ItemPreUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Output item updating: ${prevItem?.type} -> ${newItem?.type}")
    }

    private fun onOutputInventoryPostUpdate(event: ItemPostUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Output item updated: ${prevItem?.type} -> ${newItem?.type}")
    }

    private fun onWindowClose() {
        logger.info("Rerolling window closed for ${viewer.name}")
    }

    private fun onWindowOpen() {
        logger.info("Rerolling window opened for ${viewer.name}")
    }

    /**
     * 构建 [RerollingSession] 的逻辑. 返回 `null` 表示 [input] 不支持重造.
     */
    private fun createRerollingSession(
        viewer: Player,
        input: NekoStack,
    ): RerollingSession? {
        val itemType = input.key
        val itemCells = input.components.get(ItemComponentTypes.CELLS) ?: run {
            logger.error("No cells found in input item: '$input'. This is a bug!")
            return null
        }

        val itemRule = table.itemRules[itemType] ?: return null

        val cellSessionMap = SimpleRerollingSession.SelectionSessionMap()
        // TODO fill the Map

        return SimpleRerollingSession(viewer, input, cellSessionMap)
    }

    private fun createSelectionMenu(
        parentMenu: RerollingMenu,
        selectionSession: RerollingSession.SelectionSession,
    ): SelectionMenu {
        return SelectionMenu(parentMenu, selectionSession)
    }

    private fun fillSelectionGuis(guis: List<Gui>) {
        primaryGui.setContent(guis)
    }

    private fun clearSelectionGuis() {
        primaryGui.setContent(null)
    }

    private fun fillInputSlot(stack: ItemStack) {
        inputInventory.setItemSilently(0, stack)
    }

    private fun clearInputSlot() {
        inputInventory.setItemSilently(0, null)
    }

    private fun fillOutputSlot(stack: ItemStack) {
        outputInventory.setItemSilently(0, stack)
    }

    private fun getOutputSlot(): ItemStack? {
        return outputInventory.getItem(0)
    }

    private fun clearOutputSlot() {
        outputInventory.setItemSilently(0, null)
    }

    private fun editOutputSlot(block: (ItemStack) -> Unit) {
        val stack = getOutputSlot() ?: return
        fillOutputSlot(stack.apply(block))
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