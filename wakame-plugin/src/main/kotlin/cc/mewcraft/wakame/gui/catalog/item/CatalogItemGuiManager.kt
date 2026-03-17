package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.catalog.item.CatalogItemNetwork
import cc.mewcraft.wakame.catalog.item.node.*
import cc.mewcraft.wakame.craftingstation.recipe.ExpChoice
import cc.mewcraft.wakame.craftingstation.recipe.ItemChoice
import cc.mewcraft.wakame.craftingstation.recipe.ItemResult
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.SlotDisplay
import cc.mewcraft.wakame.item.resolveToItemWrapper
import cc.mewcraft.wakame.util.ReloadableProperty
import cc.mewcraft.wakame.util.runTaskTimer
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.*
import org.bukkit.scheduler.BukkitTask
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.AbstractWindow
import java.text.DecimalFormat

/**
 * 用于创建 [CatalogItemNode] 在图鉴中展示的 [Gui].
 *
 * 设计这个单例是为了前后端代码分离.
 */
internal object CatalogItemNodeGuiManager {

    /**
     * [CatalogItemNodeGui] 的构造函数.
     */
    private val GUI_CONSTRUCTORS: HashMap<Class<out CatalogItemNode>, (CatalogItemNode) -> CatalogItemNodeGui> = HashMap()

    /**
     * [CatalogItemNodeGui] 在图鉴展示时的优先级, 数字小的类型将被排在前面.
     */
    private val GUI_PRIORITIES: HashMap<Class<out CatalogItemNode>, Int> = HashMap()

    private val cachedGuis: HashMap<LookupKey, List<CatalogItemNodeGui>> by ReloadableProperty { HashMap(1024) }

    private data class LookupKey(val item: ItemRef, val state: LookupState)

    init {
        registerGuiCreator<CatalogItemBlastingNode>(::createCookingRecipeGui)
        registerGuiCreator<CatalogItemCampfireNode>(::createCookingRecipeGui)
        registerGuiCreator<CatalogItemFurnaceNode>(::createCookingRecipeGui)
        registerGuiCreator<CatalogItemShapedNode>(::createShapedRecipeGui)
        registerGuiCreator<CatalogItemShapelessNode>(::createShapelessRecipeGui)
        registerGuiCreator<CatalogItemSmithingTransformNode>(::createSmithingTransformRecipeGui)
        registerGuiCreator<CatalogItemSmithingTrimNode>(::createSmithingTrimRecipeGui)
        registerGuiCreator<CatalogItemSmokingNode>(::createCookingRecipeGui)
        registerGuiCreator<CatalogItemStonecuttingNode>(::createStonecuttingRecipeGui)
        registerGuiCreator<CatalogItemLootTableNode>(::createLootTableGui)
        registerGuiCreator<CatalogItemCraftingStationNode>(::createCraftingStationGui)
        registerGuiCreator<CatalogItemSingleSourceNode>(::createSingleSourceGui)

        registerGuiPriority<CatalogItemBlastingNode>(500)
        registerGuiPriority<CatalogItemCampfireNode>(600)
        registerGuiPriority<CatalogItemFurnaceNode>(300)
        registerGuiPriority<CatalogItemShapedNode>(100)
        registerGuiPriority<CatalogItemShapelessNode>(200)
        registerGuiPriority<CatalogItemSmithingTransformNode>(700)
        registerGuiPriority<CatalogItemSmithingTrimNode>(800)
        registerGuiPriority<CatalogItemSmokingNode>(400)
        registerGuiPriority<CatalogItemStonecuttingNode>(900)
        registerGuiPriority<CatalogItemLootTableNode>(1000)
        registerGuiPriority<CatalogItemCraftingStationNode>(1100)
        registerGuiPriority<CatalogItemSingleSourceNode>(2000)
    }

    private inline fun <reified T : CatalogItemNode> registerGuiCreator(noinline factory: (T) -> CatalogItemNodeGui) {
        GUI_CONSTRUCTORS[T::class.java] = { node -> factory(node as T) }
    }

    private inline fun <reified T : CatalogItemNode> registerGuiPriority(priority: Int) {
        GUI_PRIORITIES[T::class.java] = priority
    }

    /**
     * 根据 [ItemRef] 和 [LookupState] 获取图鉴中节点展示的 [CatalogItemNodeGui] 列表.
     */
    fun getGui(item: ItemRef, state: LookupState): List<CatalogItemNodeGui> {
        return cachedGuis.getOrPut(LookupKey(item, state)) {
            val catalogNodes = when (state) {
                LookupState.SOURCE -> CatalogItemNetwork.getSource(item)
                LookupState.USAGE -> CatalogItemNetwork.getUsage(item)
            }
            catalogNodes.sortedWith(
                // 先基于类型优先级排序, 再基于唯一标识按字典序排序
                compareBy<CatalogItemNode> { it.type.sortPriority }.thenBy { it.sortId }
            ).mapNotNull { catalogNode ->
                buildGui(catalogNode) ?: return@mapNotNull null
            }
        }
    }

    /**
     * 创建 [CatalogItemNode] 在图鉴中展示的 [CatalogItemNodeGui].
     *
     * 返回 `null` 意味着 [CatalogItemNode] 可被图鉴检索, 但代码没有指定对应 [CatalogItemNodeGui] 创建方法.
     */
    private fun buildGui(node: CatalogItemNode): CatalogItemNodeGui? {
        return GUI_CONSTRUCTORS[node::class.java]?.invoke(node).also {
            if (it == null) LOGGER.warn("No gui creator for ${node::class.java}")
        }
    }


    /**
     * 创建烧制配方 [CatalogItemNodeGui] 的方法.
     * 烧制配方包括: 熔炉, 高炉, 烟熏炉, 营火配方.
     */
    private fun createCookingRecipeGui(node: CatalogItemCookingNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = Gui.normal { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('!', CookingInfoItem(settings, node.cookingTime, node.experience))
            builder.addIngredient('f', FuelItem(settings))
            builder.addIngredient('i', DisplayItem(node.inputItems))
            builder.addIngredient('o', DisplayItem(node.outputItems, node.recipe<CookingRecipe<*>>().result.amount))
        }
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建有序合成配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createShapedRecipeGui(node: CatalogItemShapedNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.addIngredient('o', DisplayItem(node.outputItem, node.recipe<ShapedRecipe>().result.amount))

            // 凑出9个格子里的字符, 排成一个字符串后打散
            val chars = node.shape.map { it.padEnd(3, ' ') }
                .let { it + List(3 - it.size) { "   " } }
                .joinToString("")
                .toCharArray()
            // 转化为图标物品并放入gui
            builder.setContent(chars.map {
                if (it == ' ') return@map SimpleItem(ItemStack.empty())
                val itemRefs = node.inputItems[it] as List<ItemRef>
                return@map DisplayItem(itemRefs)
            })
        }
        return CatalogItemNodeGui(settings.title, gui)
    }


    /**
     * 创建无序合成配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createShapelessRecipeGui(node: CatalogItemShapelessNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.addIngredient('o', DisplayItem(node.outputItems, node.recipe<ShapelessRecipe>().result.amount))
            builder.setContent(node.inputItems.map { DisplayItem(it) })
        }
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建锻造台转化配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createSmithingTransformRecipeGui(node: CatalogItemSmithingTransformNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('b', DisplayItem(node.baseItems))
            builder.addIngredient('t', DisplayItem(node.templateItems))
            builder.addIngredient('a', DisplayItem(node.additionItems))
            builder.addIngredient('o', DisplayItem(node.outputItemRef, node.recipe<SmithingRecipe>().result.amount))
        }
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建锻造台纹饰配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createSmithingTrimRecipeGui(node: CatalogItemSmithingTrimNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('b', DisplayItem(node.baseItems))
            builder.addIngredient('t', DisplayItem(node.templateItems))
            builder.addIngredient('a', DisplayItem(node.additionItems))
            // 锻造台纹饰配方的输出槽显示一个固定物品
            builder.addIngredient('r', TrimResultItem(settings))
        }
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建切石机配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createStonecuttingRecipeGui(node: CatalogItemStonecuttingNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('i', DisplayItem(node.inputItems))
            builder.addIngredient('o', DisplayItem(node.outputItem, node.recipe<StonecuttingRecipe>().result.amount))
        }
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建战利品表 [CatalogItemNodeGui] 的方法.
     */
    private fun createLootTableGui(node: CatalogItemLootTableNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('<', PrevItem(settings))
            builder.addIngredient('>', NextItem(settings))
            builder.addIngredient('i', LootItem(node))
            builder.addIngredient('o', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.setContent(node.lootItems.map(::DisplayItem))
        }
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建合成站配方 [CatalogItemNodeGui] 的方法.
     *
     * 合成站配方是多对一的关系: 多种输入物品合成一种输出物品.
     * 输入物品展示在左侧 (i), 输出物品展示在右侧 (o).
     */
    private fun createCraftingStationGui(node: CatalogItemCraftingStationNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('<', PrevItem(settings))
            builder.addIngredient('>', NextItem(settings))
            builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.setContent(node.inputChoices.map { choice ->
                when (choice) {
                    is ItemChoice -> DisplayItem(choice.item, choice.amount)
                    is ExpChoice -> SimpleItem(ItemStack(Material.EXPERIENCE_BOTTLE, choice.amount.coerceIn(1, 64)))
                }
            })
            val output = node.outputResult
            if (output is ItemResult) {
                builder.addIngredient('o', DisplayItem(output.item, output.amount))
            } else {
                builder.addIngredient('o', SimpleItem(ItemStack.empty()))
            }
        }
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建单源节点 [CatalogItemNodeGui] 的方法.
     *
     * 单源节点的输入是一个虚拟图标 (i), 输出是手动指定的物品列表 (o).
     */
    private fun createSingleSourceGui(node: CatalogItemSingleSourceNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('?', HintItem(settings))
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('<', PrevItem(settings))
            builder.addIngredient('>', NextItem(settings))
            builder.addIngredient('i', SingleSourceItem(node))
            builder.addIngredient('o', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.setContent(node.outputItems.map(::DisplayItem))
        }
        return CatalogItemNodeGui(settings.title, gui)
    }
}

/**
 * `提示` 占位的图标.
 */
private class HintItem(
    val settings: BasicMenuSettings,
) : AbstractItem() {

    override fun getItemProvider(): ItemProvider {
        return settings.getSlotDisplay("hint").resolveToItemWrapper()
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

    }
}

/**
 * `背景` 占位的图标.
 */
private class BackgroundItem(
    val settings: BasicMenuSettings,
) : AbstractItem() {

    override fun getItemProvider(): ItemProvider {
        return settings.getSlotDisplay("background").resolveToItemWrapper()
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

    }
}

/**
 * `烧制信息` 的图标. 烧制配方使用.
 */
private class CookingInfoItem(
    val settings: BasicMenuSettings,
    val cookingTime: Int,
    val exp: Float,
) : AbstractItem() {

    override fun getItemProvider(): ItemProvider {
        return settings.getSlotDisplay("cooking_info").resolveToItemWrapper {
            standard {
                component("cooking_time", Component.text(DecimalFormat("#.#").format(cookingTime / 20.0)))
                component("exp", Component.text(DecimalFormat("#.##").format(exp)))
            }
        }
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

    }
}

/**
 * `燃料` 的图标. 烧制配方使用.
 */
private class FuelItem(
    val settings: BasicMenuSettings,
) : AbstractItem() {

    override fun getItemProvider(): ItemProvider {
        return settings.getSlotDisplay("fuel").resolveToItemWrapper()
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

    }
}

/**
 * `带纹饰的盔甲` 的图标.
 * 锻造台纹饰配方使用.
 */
private class TrimResultItem(
    val settings: BasicMenuSettings,
) : AbstractItem() {
    override fun getItemProvider(): ItemProvider {
        return settings.getSlotDisplay("trim_result").resolveToItemWrapper()
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {

    }
}

/**
 * **战利品表占位输入** 的图标.
 */
class LootItem(
    private val node: CatalogItemLootTableNode,
) : AbstractItem() {

    private val itemProvider = SlotDisplay.get(node.inputIcon).resolveToItemWrapper()

    override fun getItemProvider(): ItemProvider {
        return itemProvider
    }

    override fun handleClick(p0: ClickType, p1: Player, p2: InventoryClickEvent) {

    }
}

/**
 * **单源节点占位输入** 的图标.
 */
private class SingleSourceItem(
    private val node: CatalogItemSingleSourceNode,
) : AbstractItem() {

    private val itemProvider = SlotDisplay.get(node.inputIcon).resolveToItemWrapper()

    override fun getItemProvider(): ItemProvider {
        return itemProvider
    }

    override fun handleClick(p0: ClickType, p1: Player, p2: InventoryClickEvent) {

    }
}

/**
 * `上一页` 的图标.
 */
private class PrevItem(
    val settings: BasicMenuSettings,
) : PageItem(false) {

    override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
        if (!getGui().hasPreviousPage()) {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
        }
        return settings.getSlotDisplay("prev_page").resolveToItemWrapper {
            standard {
                component("current_page", Component.text(gui.currentPage + 1))
                component("total_page", Component.text(gui.pageAmount))
            }
        }
    }
}

/**
 * `下一页` 的图标.
 */
private class NextItem(
    val settings: BasicMenuSettings,
) : PageItem(true) {

    override fun getItemProvider(gui: PagedGui<*>): ItemProvider {
        if (!getGui().hasNextPage()) {
            return settings.getSlotDisplay("background").resolveToItemWrapper()
        }
        return settings.getSlotDisplay("next_page").resolveToItemWrapper {
            standard {
                component("current_page", Component.text(gui.currentPage + 1))
                component("total_page", Component.text(gui.pageAmount))
            }
        }
    }
}

/**
 * `配方展示物品` 的图标.
 *
 * 输入物品: 单个物品和多个物品循环.
 * 输出物品: 单个物品.
 *
 * 直接点击 = 查找该物品的获取方式.
 * Shift 点击 = 查找该物品的用途.
 */
@Suppress("FunctionName")
private fun DisplayItem(items: List<ItemRef>, amount: Int = 1): AbstractItem {
    if (items.isEmpty())
        return SimpleItem(ItemStack.empty())
    return if (items.size == 1) {
        DisplayItem(items.first(), amount)
    } else {
        MultiDisplayItem(items, amount)
    }
}

@Suppress("FunctionName")
private fun DisplayItem(items: ItemRef, amount: Int = 1): AbstractItem {
    return SingleDisplayItem(items, amount)
}

/**
 * 方便函数.
 */
private fun handleClickCommons(clickType: ClickType, player: Player, item: ItemRef) {
    val lookupState = when (clickType) {
        ClickType.LEFT, ClickType.RIGHT -> LookupState.SOURCE
        ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> LookupState.USAGE
        else -> return
    }

    // 图鉴菜单队列过长时, 不再打开新的
    if (CatalogItemMenuStacks.size(player) > 50) {
        player.sendMessage(TranslatableMessages.MSG_CATALOG_MENU_DEQUE_LIMIT)
        return
    }

    // 要打开的菜单和当前菜单一模一样, 不再打开新的
    val currentMenu = CatalogItemMenuStacks.peek(player)
    if (currentMenu is CatalogItemFocusMenu && currentMenu.item == item && currentMenu.state == lookupState) {
        return
    }

    // 要打开的菜单列表为空, 则不打开
    val catalogNodeGuis = CatalogItemNodeGuiManager.getGui(item, lookupState)
    if (catalogNodeGuis.isEmpty())
        return

    CatalogItemMenuStacks.push(player, CatalogItemFocusMenu(item, lookupState, player, catalogNodeGuis))
}

private class SingleDisplayItem(
    val item: ItemRef,
    val amount: Int,
) : AbstractItem() {

    override fun getItemProvider(): ItemProvider {
        return ItemWrapper(item.createItemStack(amount))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        handleClickCommons(clickType, player, item)
    }
}

private class MultiDisplayItem(
    val items: List<ItemRef>,
    /**
     * 对于循环物品, 所有物品数量相同.
     */
    val amount: Int,
) : AbstractItem() {

    private var task: BukkitTask? = null
    private var state = 0

    fun start() {
        task?.cancel()
        // 物品循环周期固定为 20t
        task = runTaskTimer(0L, 20L, ::cycle)
    }

    fun cancel() {
        task?.cancel()
        task = null
    }

    private fun cycle() {
        ++state
        if (state == items.size) {
            state = 0
        }
        notifyWindows()
    }

    override fun getItemProvider(): ItemProvider {
        return ItemWrapper(items[state].createItemStack(amount))
    }

    override fun addWindow(window: AbstractWindow) {
        super.addWindow(window)
        if (task == null) {
            start()
        }
    }

    override fun removeWindow(window: AbstractWindow) {
        super.removeWindow(window)
        if (windows.isEmpty() && task != null) {
            cancel()
        }
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        handleClickCommons(clickType, player, items[state])
    }
}

internal enum class LookupState {
    /**
     * 代表检索的是物品的来源.
     */
    SOURCE,
    /**
     * 代表检索的是物品的用途.
     */
    USAGE
}

/**
 * 封装了标题和对应的 [Gui].
 */
internal data class CatalogItemNodeGui(
    val title: Component,
    val gui: Gui,
)