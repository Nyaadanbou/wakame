package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.catalog.item.CatalogItemMenuSettings
import cc.mewcraft.wakame.catalog.item.CatalogItemRecipeNetwork
import cc.mewcraft.wakame.catalog.item.recipe.*
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.item.SlotDisplay
import cc.mewcraft.wakame.item.resolveToItemWrapper
import cc.mewcraft.wakame.util.ReloadableProperty
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.*
import org.bukkit.scheduler.BukkitTask
import xyz.xenondevs.invui.InvUI
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
 * 用于创建 [CatalogRecipe] 在图鉴中展示的 [Gui].
 *
 * 设计这个单例是为了前后端代码分离.
 */
internal object CatalogRecipeGuiManager {

    private val GUI_CREATORS: HashMap<Class<out CatalogRecipe>, (CatalogRecipe) -> CatalogRecipeGui> = HashMap()

    /**
     * [CatalogRecipe] 的 [Gui] 在图鉴展示时的优先级, 数字小的类型将被排在前面.
     */
    private val GUI_PRIORITIES: HashMap<Class<out CatalogRecipe>, Int> = HashMap()

    private val CACHED_GUIS: HashMap<LookupKey, List<CatalogRecipeGui>> by ReloadableProperty { HashMap(1024) }

    private data class LookupKey(val item: ItemRef, val state: LookupState)

    init {
        registerGuiCreator<CatalogBlastingRecipe>(::createCookingRecipeGui)
        registerGuiCreator<CatalogCampfireRecipe>(::createCookingRecipeGui)
        registerGuiCreator<CatalogFurnaceRecipe>(::createCookingRecipeGui)
        registerGuiCreator<CatalogShapedRecipe>(::createShapedRecipeGui)
        registerGuiCreator<CatalogShapelessRecipe>(::createShapelessRecipeGui)
        registerGuiCreator<CatalogSmithingTransformRecipe>(::createSmithingTransformRecipeGui)
        registerGuiCreator<CatalogSmithingTrimRecipe>(::createSmithingTrimRecipeGui)
        registerGuiCreator<CatalogSmokingRecipe>(::createCookingRecipeGui)
        registerGuiCreator<CatalogStonecuttingRecipe>(::createStonecuttingRecipeGui)
        registerGuiCreator<CatalogItemLootTableRecipe>(::createLootTableRecipeGui)
    }

    init {
        // TODO 支持配置文件载入优先级
        registerGuiPriority<CatalogBlastingRecipe>(500)
        registerGuiPriority<CatalogCampfireRecipe>(600)
        registerGuiPriority<CatalogFurnaceRecipe>(300)
        registerGuiPriority<CatalogShapedRecipe>(100)
        registerGuiPriority<CatalogShapelessRecipe>(200)
        registerGuiPriority<CatalogSmithingTransformRecipe>(700)
        registerGuiPriority<CatalogSmithingTrimRecipe>(800)
        registerGuiPriority<CatalogSmokingRecipe>(400)
        registerGuiPriority<CatalogStonecuttingRecipe>(900)
    }

    private inline fun <reified T : CatalogRecipe> registerGuiCreator(noinline factory: (T) -> CatalogRecipeGui) {
        GUI_CREATORS[T::class.java] = { recipe -> factory(recipe as T) }
    }

    private inline fun <reified T : CatalogRecipe> registerGuiPriority(priority: Int) {
        GUI_PRIORITIES[T::class.java] = priority
    }

    /**
     * 根据 [ItemRef] 和 [LookupState] 获取图鉴中配方展示的 [CatalogRecipeGui] 列表.
     */
    fun getGui(item: ItemRef, state: LookupState): List<CatalogRecipeGui> {
        return CACHED_GUIS.getOrPut(LookupKey(item, state)) {
            val catalogRecipes = when (state) {
                LookupState.SOURCE -> CatalogItemRecipeNetwork.getSource(item)
                LookupState.USAGE -> CatalogItemRecipeNetwork.getUsage(item)
            }
            // 先基于类型优先级排序
            // 再基于唯一标识按字典序排序
            catalogRecipes.sortedWith(
                compareBy<CatalogRecipe> { it.type.sortPriority }.thenBy { it.sortId }
            ).mapNotNull { catalogRecipe ->
                buildGui(catalogRecipe) ?: return@mapNotNull null
            }
        }
    }

    /**
     * 创建 [CatalogRecipe] 在图鉴中展示的 [CatalogRecipeGui].
     *
     * 返回 `null` 意味着 [CatalogRecipe] 可被图鉴检索, 但代码没有指定对应 [CatalogRecipeGui] 创建方法.
     */
    private fun buildGui(recipe: CatalogRecipe): CatalogRecipeGui? = GUI_CREATORS[recipe::class.java]?.invoke(recipe).also {
        if (it == null) LOGGER.warn("No gui creator for ${recipe::class.java}")
    }

    /**
     * 方便函数.
     */
    private val CatalogStandardRecipe.menuSettings: BasicMenuSettings
        get() = CatalogItemMenuSettings.getMenuSettings(this.type.name)

    /**
     * 创建烧制配方 [CatalogRecipeGui] 的方法.
     * 烧制配方包括: 熔炉, 高炉, 烟熏炉, 营火配方.
     */
    private fun createCookingRecipeGui(catalogRecipe: CatalogCookingRecipe): CatalogRecipeGui {
        val settings = catalogRecipe.menuSettings
        val gui = Gui.normal { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('!', CookingInfoItem(settings, catalogRecipe.cookingTime, catalogRecipe.experience))
            builder.addIngredient('f', FuelItem(settings))
            builder.addIngredient('i', DisplayItem(catalogRecipe.inputItems))
            builder.addIngredient('o', DisplayItem(catalogRecipe.outputItems, catalogRecipe.recipe<CookingRecipe<*>>().result.amount))
        }
        return CatalogRecipeGui(settings.title, gui)
    }

    /**
     * 创建有序合成配方 [CatalogRecipeGui] 的方法.
     */
    private fun createShapedRecipeGui(catalogRecipe: CatalogShapedRecipe): CatalogRecipeGui {
        val settings = catalogRecipe.menuSettings
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.addIngredient('o', DisplayItem(catalogRecipe.outputItem, catalogRecipe.recipe<ShapedRecipe>().result.amount))

            // 凑出9个格子里的字符, 排成一个字符串后打散
            val chars = catalogRecipe.shape.map { it.padEnd(3, ' ') }
                .let { it + List(3 - it.size) { "   " } }
                .joinToString("")
                .toCharArray()
            // 转化为图标物品并放入gui
            builder.setContent(chars.map {
                if (it == ' ') return@map SimpleItem(ItemStack.empty())
                val itemRefs = catalogRecipe.inputItems[it] as List<ItemRef>
                return@map DisplayItem(itemRefs)
            })
        }
        return CatalogRecipeGui(settings.title, gui)
    }


    /**
     * 创建无序合成配方 [CatalogRecipeGui] 的方法.
     */
    private fun createShapelessRecipeGui(catalogRecipe: CatalogShapelessRecipe): CatalogRecipeGui {
        val settings = catalogRecipe.menuSettings
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.addIngredient('o', DisplayItem(catalogRecipe.outputItems, catalogRecipe.recipe<ShapelessRecipe>().result.amount))
            builder.setContent(catalogRecipe.inputItems.map { DisplayItem(it) })
        }
        return CatalogRecipeGui(settings.title, gui)
    }

    /**
     * 创建锻造台转化配方 [CatalogRecipeGui] 的方法.
     */
    private fun createSmithingTransformRecipeGui(catalogRecipe: CatalogSmithingTransformRecipe): CatalogRecipeGui {
        val settings = catalogRecipe.menuSettings
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('b', DisplayItem(catalogRecipe.baseItems))
            builder.addIngredient('t', DisplayItem(catalogRecipe.templateItems))
            builder.addIngredient('a', DisplayItem(catalogRecipe.additionItems))
            builder.addIngredient('o', DisplayItem(catalogRecipe.outputItemRef, catalogRecipe.recipe<SmithingRecipe>().result.amount))
        }
        return CatalogRecipeGui(settings.title, gui)
    }

    /**
     * 创建锻造台纹饰配方 [CatalogRecipeGui] 的方法.
     */
    private fun createSmithingTrimRecipeGui(catalogRecipe: CatalogSmithingTrimRecipe): CatalogRecipeGui {
        val settings = catalogRecipe.menuSettings
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('b', DisplayItem(catalogRecipe.baseItems))
            builder.addIngredient('t', DisplayItem(catalogRecipe.templateItems))
            builder.addIngredient('a', DisplayItem(catalogRecipe.additionItems))
            // 锻造台纹饰配方的输出槽显示一个固定物品
            builder.addIngredient('r', TrimResultItem(settings))
        }
        return CatalogRecipeGui(settings.title, gui)
    }

    /**
     * 创建切石机配方 [CatalogRecipeGui] 的方法.
     */
    private fun createStonecuttingRecipeGui(catalogRecipe: CatalogStonecuttingRecipe): CatalogRecipeGui {
        val settings = catalogRecipe.menuSettings
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('i', DisplayItem(catalogRecipe.inputItems))
            builder.addIngredient('o', DisplayItem(catalogRecipe.outputItem, catalogRecipe.recipe<StonecuttingRecipe>().result.amount))
        }
        return CatalogRecipeGui(settings.title, gui)
    }

    /**
     * 创建战利品表配方 [CatalogRecipeGui] 的方法.
     */
    private fun createLootTableRecipeGui(catalogRecipe: CatalogItemLootTableRecipe): CatalogRecipeGui {
        val settings = catalogRecipe.catalogMenuSettings
        val gui = PagedGui.items { builder ->
            builder.setStructure(*settings.structure)
            builder.addIngredient('.', BackgroundItem(settings))
            builder.addIngredient('<', PrevItem(settings))
            builder.addIngredient('>', NextItem(settings))
            builder.addIngredient('i', LootItem(catalogRecipe))
            builder.addIngredient('o', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            builder.setContent(catalogRecipe.lootItems.map(::DisplayItem))
        }
        return CatalogRecipeGui(settings.title, gui)
    }

}

/**
 * `背景` 占位的图标.
 */
private class BackgroundItem(
    val settings: BasicMenuSettings,
) : AbstractItem() {
    override fun getItemProvider(): ItemProvider = settings.getSlotDisplay("background").resolveToItemWrapper()
    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Unit
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

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Unit
}

/**
 * `燃料` 的图标. 烧制配方使用.
 */
private class FuelItem(
    val settings: BasicMenuSettings,
) : AbstractItem() {
    override fun getItemProvider(): ItemProvider = settings.getSlotDisplay("fuel").resolveToItemWrapper()
    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Unit
}

/**
 * `带纹饰的盔甲` 的图标.
 * 锻造台纹饰配方使用.
 */
private class TrimResultItem(
    val settings: BasicMenuSettings,
) : AbstractItem() {
    override fun getItemProvider(): ItemProvider = settings.getSlotDisplay("trim_result").resolveToItemWrapper()
    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) = Unit
}

/**
 * **战利品表占位输入** 的图标.
 */
class LootItem(
    private val lootTableRecipe: CatalogItemLootTableRecipe,
) : AbstractItem() {
    private val itemProvider: ItemProvider = SlotDisplay.get(lootTableRecipe.catalogIcon).resolveToItemWrapper()
    override fun getItemProvider(): ItemProvider = itemProvider
    override fun handleClick(p0: ClickType, p1: Player, p2: InventoryClickEvent) = Unit
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
        return settings.getSlotDisplay("prev_page").resolveToItemWrapper()
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
        return settings.getSlotDisplay("next_page").resolveToItemWrapper()
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
private fun DisplayItem(items: List<ItemRef>, amount: Int = 1): AbstractItem {
    if (items.isEmpty()) return SimpleItem(ItemStack.empty())
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

private class SingleDisplayItem(
    val item: ItemRef,
    val amount: Int,
) : AbstractItem() {
    override fun getItemProvider(): ItemProvider {
        // TODO 渲染
        return ItemWrapper(item.createItemStack(amount))
    }

    override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
        handleClick0(clickType, player, item)
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
        task = Bukkit.getScheduler().runTaskTimer(InvUI.getInstance().plugin, ::cycle, 0L, 20L)
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
        // TODO 渲染
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
        handleClick0(clickType, player, items[state])
    }
}

/**
 * 方便函数.
 */
private fun AbstractItem.handleClick0(clickType: ClickType, player: Player, item: ItemRef) {
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
    val catalogRecipeGuis = CatalogRecipeGuiManager.getGui(item, lookupState)
    if (catalogRecipeGuis.isEmpty())
        return

    CatalogItemMenuStacks.push(player, CatalogItemFocusMenu(item, lookupState, player, catalogRecipeGuis))
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
internal data class CatalogRecipeGui(
    val title: Component,
    val gui: Gui,
)