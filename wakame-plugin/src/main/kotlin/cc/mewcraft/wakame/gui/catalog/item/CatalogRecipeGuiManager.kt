package cc.mewcraft.wakame.gui.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.catalog.item.*
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.gui.catalog.item.menu.PagedCatalogRecipesMenu
import cc.mewcraft.wakame.item.ItemStacks
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

    private val GUI_CREATORS: HashMap<Class<out CatalogRecipe>, (CatalogRecipe) -> Gui> = HashMap()

    /**
     * [CatalogRecipe] 的 [Gui] 在图鉴展示时的优先级, 数字小的将被排在前面.
     */
    private val GUI_PRIORITIES: HashMap<Class<out CatalogRecipe>, Int> = HashMap()

    private val CACHED_GUIS: HashMap<LookupKey, List<CatalogRecipeGui>> by ReloadableProperty { HashMap(128) }

    private data class LookupKey(val item: ItemX, val state: LookupState)

    init {
        registerGuiCreator<BlastingRecipeAdapter>(::createCookingRecipeGui)
        registerGuiCreator<CampfireRecipeAdapter>(::createCookingRecipeGui)
        registerGuiCreator<FurnaceRecipeAdapter>(::createCookingRecipeGui)
        registerGuiCreator<ShapedRecipeAdapter>(::createShapedRecipeGui)
        registerGuiCreator<ShapelessRecipeAdapter>(::createShapelessRecipeGui)
        registerGuiCreator<SmithingTransformRecipeAdapter>(::createSmithingTransformRecipeGui)
        registerGuiCreator<SmithingTrimRecipeAdapter>(::createSmithingTrimRecipeGui)
        registerGuiCreator<SmokingRecipeAdapter>(::createCookingRecipeGui)
        registerGuiCreator<StonecuttingRecipeAdapter>(::createStonecuttingRecipeGui)
    }

    init {
        // TODO 支持配置文件载入优先级
        registerGuiPriority<BlastingRecipeAdapter>(500)
        registerGuiPriority<CampfireRecipeAdapter>(600)
        registerGuiPriority<FurnaceRecipeAdapter>(300)
        registerGuiPriority<ShapedRecipeAdapter>(100)
        registerGuiPriority<ShapelessRecipeAdapter>(200)
        registerGuiPriority<SmithingTransformRecipeAdapter>(700)
        registerGuiPriority<SmithingTrimRecipeAdapter>(800)
        registerGuiPriority<SmokingRecipeAdapter>(400)
        registerGuiPriority<StonecuttingRecipeAdapter>(900)
    }

    private inline fun <reified T : CatalogRecipe> registerGuiCreator(noinline factory: (T) -> Gui) {
        GUI_CREATORS[T::class.java] = { recipe -> factory(recipe as T) }
    }

    private inline fun <reified T : CatalogRecipe> registerGuiPriority(priority: Int) {
        GUI_PRIORITIES[T::class.java] = priority
    }

    /**
     * 根据 [ItemX] 和 [LookupState] 获取图鉴中配方展示的 [CatalogRecipeGui] 列表.
     */
    fun getCatalogRecipeGuis(item: ItemX, state: LookupState): List<CatalogRecipeGui> = CACHED_GUIS.getOrPut(LookupKey(item, state)) {
        val catalogRecipes = when (state) {
            LookupState.SOURCE -> CatalogRecipeNetwork.getSource(item)
            LookupState.USAGE -> CatalogRecipeNetwork.getUsage(item)
        }
        // 基于类型排序
        catalogRecipes.sortedBy {
            GUI_PRIORITIES[it::class.java] ?: Int.MAX_VALUE
        }.mapNotNull {
            val gui = buildGui(it) ?: return@mapNotNull null
            CatalogRecipeGui(it.type, gui)
        }
    }

    /**
     * 创建 [CatalogRecipe] 在图鉴中展示的 [Gui].
     *
     * 返回 `null` 意味着 [CatalogRecipe] 可被图鉴检索, 但代码没有指定对应 [Gui] 创建方法.
     */
    private fun buildGui(recipe: CatalogRecipe): Gui? = GUI_CREATORS[recipe::class.java]?.invoke(recipe).also {
        if (it == null) LOGGER.warn("No gui creator for ${recipe::class.java}")
    }

    /**
     * 创建烧制配方 [Gui] 的方法.
     * 烧制配方包括: 熔炉, 高炉, 烟熏炉, 营火配方.
     */
    private fun createCookingRecipeGui(adapter: CookingRecipeAdapter): Gui = Gui.normal { builder ->
        val settings: BasicMenuSettings = getMenuSettings(adapter)
        val recipe: CookingRecipe<*> = adapter.recipe()

        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem(settings))
        builder.addIngredient('!', CookingInfoItem(settings, recipe.cookingTime, recipe.experience))
        builder.addIngredient('f', FuelItem(settings))
        builder.addIngredient('i', DisplayItem(adapter.inputItems))
        builder.addIngredient('o', DisplayItem(adapter.outputItems, recipe.result.amount))
    }

    /**
     * 创建有序合成配方 [Gui] 的方法.
     */
    private fun createShapedRecipeGui(adapter: ShapedRecipeAdapter): Gui = PagedGui.items { builder ->
        val settings: BasicMenuSettings = getMenuSettings(adapter)
        val recipe: ShapedRecipe = adapter.recipe()

        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem(settings))
        builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('o', DisplayItem(adapter.outputItem, recipe.result.amount))

        // 凑出 9 个格子里的字符, 排成一个字符串后打散
        val chars = recipe.shape.map { it.padEnd(3, ' ') }
            .let { it + List(3 - it.size) { "   " } }
            .joinToString("")
            .toCharArray()

        // 转化为图标物品并放入 GUI
        builder.setContent(chars.map {
            if (it == ' ') return@map SimpleItem(ItemStack.empty())
            val items = adapter.inputItems[it] as List<ItemX>
            return@map if (items.isEmpty()) {
                SimpleItem(ItemStack.empty())
            } else {
                DisplayItem(items)
            }
        })
    }

    /**
     * 创建无序合成配方 [Gui] 的方法.
     */
    private fun createShapelessRecipeGui(adapter: ShapelessRecipeAdapter): Gui = PagedGui.items { builder ->
        val settings: BasicMenuSettings = getMenuSettings(adapter)
        val recipe: ShapelessRecipe = adapter.recipe()

        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem(settings))
        builder.addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
        builder.addIngredient('o', DisplayItem(adapter.outputItems, recipe.result.amount))
        builder.setContent(adapter.inputItems.map { DisplayItem(it) })
    }

    /**
     * 创建锻造台转化配方 [Gui] 的方法.
     */
    private fun createSmithingTransformRecipeGui(adapter: SmithingTransformRecipeAdapter): Gui = PagedGui.items { builder ->
        val settings: BasicMenuSettings = getMenuSettings(adapter)
        val recipe: SmithingRecipe = adapter.recipe()

        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem(settings))
        builder.addIngredient('b', DisplayItem(adapter.baseItems))
        builder.addIngredient('t', DisplayItem(adapter.templateItems))
        builder.addIngredient('a', DisplayItem(adapter.additionItems))
        builder.addIngredient('o', DisplayItem(adapter.outputItemX, recipe.result.amount))
    }

    /**
     * 创建锻造台纹饰配方 [Gui] 的方法.
     */
    private fun createSmithingTrimRecipeGui(adapter: SmithingTrimRecipeAdapter): Gui = PagedGui.items { builder ->
        val settings: BasicMenuSettings = getMenuSettings(adapter)

        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem(settings))
        builder.addIngredient('b', DisplayItem(adapter.baseItems))
        builder.addIngredient('t', DisplayItem(adapter.templateItems))
        builder.addIngredient('a', DisplayItem(adapter.additionItems))
        // 锻造台纹饰配方的输出槽显示一个固定物品
        builder.addIngredient('r', TrimResultItem(settings))
    }

    /**
     * 创建切石机配方 [Gui] 的方法.
     */
    private fun createStonecuttingRecipeGui(adapter: StonecuttingRecipeAdapter): Gui = PagedGui.items { builder ->
        val settings: BasicMenuSettings = getMenuSettings(adapter)
        val recipe: StonecuttingRecipe = adapter.recipe()

        builder.setStructure(*settings.structure)
        builder.addIngredient('.', BackgroundItem(settings))
        builder.addIngredient('i', DisplayItem(adapter.inputItems))
        builder.addIngredient('o', DisplayItem(adapter.outputItem, recipe.result.amount))
    }

    /**
     * 方便函数.
     */
    private fun getMenuSettings(adapter: BukkitRecipeAdapter): BasicMenuSettings {
        return ItemCatalogInitializer.getMenuSettings(adapter.type.name.lowercase())
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
@Suppress("FunctionName")
private fun DisplayItem(items: List<ItemX>, amount: Int = 1): AbstractItem {
    if (items.isEmpty())
        return SimpleItem(ItemStacks.createUnknown("Recipe input is empty"))
    return if (items.size == 1) {
        DisplayItem(items.first(), amount)
    } else {
        MultiDisplayItem(items, amount)
    }
}

@Suppress("FunctionName")
private fun DisplayItem(items: ItemX, amount: Int = 1): AbstractItem {
    return SingleDisplayItem(items, amount)
}

private class SingleDisplayItem(
    val item: ItemX,
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
    val items: List<ItemX>,
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
private fun AbstractItem.handleClick0(clickType: ClickType, player: Player, item: ItemX) {
    val lookupState = when (clickType) {
        ClickType.LEFT, ClickType.RIGHT -> LookupState.SOURCE
        ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT -> LookupState.USAGE
        else -> return
    }
    val menuList = ItemCatalogMenuStack.get(player)

    // 图鉴菜单队列过长时, 不再打开新的
    if (menuList.size > 50) {
        player.sendMessage(TranslatableMessages.MSG_CATALOG_MENU_DEQUE_LIMIT)
        return
    }

    // 要打开的菜单和当前菜单一模一样, 不再打开新的
    if (menuList.isNotEmpty()) {
        val currentMenu = menuList.first()
        if (currentMenu is PagedCatalogRecipesMenu && currentMenu.item == item && currentMenu.lookupState == lookupState)
            return
    }

    // 要打开的菜单Gui列表为空，则不打开
    val catalogRecipeGuis = CatalogRecipeGuiManager.getCatalogRecipeGuis(item, lookupState)
    if (catalogRecipeGuis.isEmpty())
        return

    ItemCatalogMenuStack.push(player, PagedCatalogRecipesMenu(item, lookupState, player, catalogRecipeGuis))
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
 * 封装了 [CatalogRecipeType] 和对应的 [Gui].
 */
internal data class CatalogRecipeGui(
    val type: CatalogRecipeType,
    val gui: Gui,
)