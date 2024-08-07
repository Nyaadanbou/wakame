package cc.mewcraft.wakame.gui.rerolling

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
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
import kotlin.properties.Delegates

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
        primaryWindow.open(viewer)
    }

    /**
     * 基于 [rerollingSession] 的当前状态执行一次重造, 并刷新输出容器.
     */
    fun refreshOutput() {
        val session = rerollingSession ?: run {
            logger.info("Trying to refresh output without a session.")
            return
        }

        if (session.frozen) {
            logger.error("Trying to refresh output in a frozen session. This is a bug!")
            return
        }

        // 执行一次重造
        val result = session.reforge()

        // 渲染输出物品
        val resultItem = result.item
        val renderer = OutputItemRenderer(result)
        renderer.render(resultItem)

        // 填充输出容器
        editOutputSlot { resultItem.unsafe.handle }
    }

    private val logger: Logger by inject()

    //<editor-fold desc="InvUI components">
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
    //</editor-fold>

    var rerollingSession: RerollingSession? by Delegates.observable(null) { _, _, new ->
        if (new == null) {
            logger.info("Rerolling session cleared.")
        } else {
            logger.info("Rerolling session set: ${new.inputItem.key}")
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
            // 玩家交换物品:
            // 禁止该操作.
            event.isSwap -> {
                viewer.sendPlainMessage("猫咪不可以!")
                event.isCancelled = true; return
            }

            // 玩家把物品放进输入容器:
            // 这时候我们需要创建一个新的‘重造会话’,
            // 然后把所有(可重造的)词条栏图标呈现在菜单里,
            // 最后刷新一下输出容器里的图标.
            event.isAdd -> {
                val ns = newItem?.tryNekoStack ?: run {
                    viewer.sendPlainMessage("请放入一个萌芽物品!")
                    event.isCancelled = true; return
                }

                if (!ns.components.has(ItemComponentTypes.LEVEL)) {
                    viewer.sendPlainMessage("请放入一个拥有等级的物品!")
                    event.isCancelled = true; return
                }

                if (!ns.components.has(ItemComponentTypes.CELLS)) {
                    viewer.sendPlainMessage("请放入一个拥有词条栏的物品!")
                    event.isCancelled = true; return
                }

                val session = createRerollSession(viewer, ns) ?: run {
                    viewer.sendPlainMessage("该物品不支持重造!")
                    event.isCancelled = true; return
                }

                // 设置会话
                rerollingSession = session

                // 将*克隆*放入输入容器
                event.newItem = ns.itemStack

                val selectionMenus = session.selections.map { (_, selection) ->
                    createSelectionMenu(this, selection)
                }
                val selectionGuis = selectionMenus.map {
                    it.primaryGui
                }
                fillSelectionGuis(selectionGuis)

                // 更新输出容器
                refreshOutput()
            }

            // 玩家从输入容器拿出物品:
            // 说明玩家中途放弃了这次重造.
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

        when {
            // 玩家尝试交换物品:
            // 禁止该操作.
            event.isSwap -> {
                event.isCancelled = true
                viewer.sendPlainMessage("不支持交换物品.")
            }

            // 玩家把物品放进输出容器:
            // 禁止该操作.
            event.isAdd -> {
                event.isCancelled = true
                viewer.sendPlainMessage("不支持放入物品.")
            }

            // 玩家从输出容器拿出物品:
            // 说明玩家想要完成这次重造.
            event.isRemove -> {
                event.isCancelled = true

                val session = rerollingSession ?: run {
                    logger.error("Rerolling session (viewer: ${viewer.name}) is null, but output item is being removed. This is a bug!")
                    return
                }

                // 首先获得当前的重造结果
                val result = session.result
                if (!result.cost.test(viewer)) {
                    viewer.sendPlainMessage("你没有足够的货币!")
                    return
                }

                // 把重造后的物品给玩家
                viewer.inventory.addItem(result.item.unsafe.handle)

                clearOutputSlot()
                clearSelectionGuis()

                session.frozen = true
                rerollingSession = null
            }
        }
    }

    private fun onOutputInventoryPostUpdate(event: ItemPostUpdateEvent) {
        val newItem = event.newItem
        val prevItem = event.previousItem
        logger.info("Output item updated: ${prevItem?.type} -> ${newItem?.type}")
    }

    private fun onWindowClose() {
        logger.info("Rerolling window closed for ${viewer.name}")

        val session = rerollingSession ?: run {
            logger.info("The window of reroll menu is closed while session being null.")
            return
        }

        clearOutputSlot()
        clearSelectionGuis()
        session.returnInput(viewer)
    }

    private fun onWindowOpen() {
        logger.info("Rerolling window opened for ${viewer.name}")
    }

    /**
     * 构建 [RerollingSession] 的逻辑.
     *
     * 返回 `null` 表示 [inputItem] 不支持重造.
     */
    private fun createRerollSession(
        viewer: Player,
        inputItem: NekoStack,
    ): RerollingSession? {
        // 如果这个物品没有对应的词条栏模板, 则判定整个物品不支持重造
        val templateCellMap = inputItem.templates.get(ItemTemplateTypes.CELLS)?.cells ?: run {
            logger.warn("Input item has no cell templates. This might be a design issue.")
            return null
        }

        val itemCells = inputItem.components.get(ItemComponentTypes.CELLS)!!

        // 如果这个物品没有对应的重造规则, 则判定整个物品不支持重造
        val itemRule = table.itemRules[inputItem.key] ?: return null

        val selectionMap = SimpleRerollingSession.SelectionMap()
        for ((id, cell) in itemCells) {

            // 如果这个词条栏没有对应的重造规则, 则判定该词条栏不支持重造
            val cellRule = itemRule.cellRules[id] ?: continue

            val display = SimpleRerollingSession.Selection.Display(
                name = cell.provideTooltipName().content,
                lore = cell.provideTooltipLore().content,
            )

            // 这个词条栏没有对应的模板, 则判定该词条栏不支持重造
            val coreTemplate = templateCellMap[id]?.core ?: continue

            val selection = SimpleRerollingSession.Selection(
                id = id,
                rule = cellRule,
                group = coreTemplate,
                display = display,
            )
            selectionMap[id] = selection
        }

        return SimpleRerollingSession(table, viewer, inputItem, selectionMap)
    }

    private fun createSelectionMenu(
        parentMenu: RerollingMenu,
        selection: RerollingSession.Selection,
    ): SelectionMenu {
        return SelectionMenu(parentMenu, selection)
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