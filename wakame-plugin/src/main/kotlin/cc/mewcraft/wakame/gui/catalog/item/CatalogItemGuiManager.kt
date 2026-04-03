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
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.*
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemWrapper
import java.text.DecimalFormat

/**
 * 用于创建 [CatalogItemNode] 在图鉴中展示的 [Gui].
 *
 * 设计这个单例是为了前后端代码分离.
 */
internal object CatalogItemNodeGuiManager {

    /** [CatalogItemNodeGui] 的构造函数. */
    private val GUI_CONSTRUCTORS: HashMap<Class<out CatalogItemNode>, (CatalogItemNode) -> CatalogItemNodeGui> = HashMap()
    /** [CatalogItemNodeGui] 在图鉴展示时的优先级, 数字小的类型排在前面. */
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
        val gui = Gui.builder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('!', CookingInfoItem(settings, node.cookingTime, node.experience))
            .addIngredient('f', FuelItem(settings))
            .addIngredient('i', DisplayItem(node.inputItems))
            .addIngredient('o', DisplayItem(node.outputItems, node.recipe<CookingRecipe<*>>().result.amount))
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建有序合成配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createShapedRecipeGui(node: CatalogItemShapedNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('o', DisplayItem(node.outputItem, node.recipe<ShapedRecipe>().result.amount))
            .setContent(run {
                // 凑出9个格子里的字符, 排成一个字符串后打散
                val chars = node.shape.map { it.padEnd(3, ' ') }
                    .let { it + List(3 - it.size) { "   " } }
                    .joinToString("")
                    .toCharArray()
                // 转化为图标物品并放入gui
                chars.map {
                    if (it == ' ') return@map Item.simple(ItemStack.empty())
                    val itemRefs = node.inputItems[it] as List<ItemRef>
                    return@map DisplayItem(itemRefs)
                }
            })
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }


    /**
     * 创建无序合成配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createShapelessRecipeGui(node: CatalogItemShapelessNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('o', DisplayItem(node.outputItems, node.recipe<ShapelessRecipe>().result.amount))
            .setContent(node.inputItems.map {
                DisplayItem(it)
            })
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建锻造台转化配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createSmithingTransformRecipeGui(node: CatalogItemSmithingTransformNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('b', DisplayItem(node.baseItems))
            .addIngredient('t', DisplayItem(node.templateItems))
            .addIngredient('a', DisplayItem(node.additionItems))
            .addIngredient('o', DisplayItem(node.outputItemRef, node.recipe<SmithingRecipe>().result.amount))
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建锻造台纹饰配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createSmithingTrimRecipeGui(node: CatalogItemSmithingTrimNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('b', DisplayItem(node.baseItems))
            .addIngredient('t', DisplayItem(node.templateItems))
            .addIngredient('a', DisplayItem(node.additionItems))
            .addIngredient(
                'r', Item.builder()
                    .setItemProvider { _ ->
                        settings.getIcon("trim_result").resolveToItemWrapper()
                    }
            )
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建切石机配方 [CatalogItemNodeGui] 的方法.
     */
    private fun createStonecuttingRecipeGui(node: CatalogItemStonecuttingNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('i', DisplayItem(node.inputItems))
            .addIngredient('o', DisplayItem(node.outputItem, node.recipe<StonecuttingRecipe>().result.amount))
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建战利品表 [CatalogItemNodeGui] 的方法.
     */
    private fun createLootTableGui(node: CatalogItemLootTableNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('<', PrevPageItem(settings))
            .addIngredient('>', NextPageItem(settings))
            .addIngredient('i', Item.simple(SlotDisplay.get(node.inputIcon).resolveToItemWrapper()))
            .addIngredient('o', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setContent(node.lootItems.map(::DisplayItem))
            .build()
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
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('<', PrevPageItem(settings))
            .addIngredient('>', NextPageItem(settings))
            .addIngredient('i', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setContent(node.inputChoices.map { choice ->
                when (choice) {
                    is ItemChoice -> DisplayItem(choice.item, choice.amount)
                    is ExpChoice -> Item.simple(ItemStack(Material.EXPERIENCE_BOTTLE, choice.amount.coerceIn(1, 64)))
                }
            })
            .apply {
                val output = node.outputResult
                if (output is ItemResult) {
                    addIngredient('o', DisplayItem(output.item, output.amount))
                } else {
                    addIngredient('o', Item.simple(ItemStack.empty()))
                }
            }
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }

    /**
     * 创建单源节点 [CatalogItemNodeGui] 的方法.
     *
     * 单源节点的输入是一个虚拟图标 (i), 输出是手动指定的物品列表 (o).
     */
    private fun createSingleSourceGui(node: CatalogItemSingleSourceNode): CatalogItemNodeGui {
        val settings = node.menuCfg
        val gui = PagedGui.itemsBuilder()
            .setStructure(*settings.structure)
            .addIngredient('?', HintItem(settings))
            .addIngredient('.', BackgroundItem(settings))
            .addIngredient('<', PrevPageItem(settings))
            .addIngredient('>', NextPageItem(settings))
            .addIngredient('i', Item.simple(SlotDisplay.get(node.inputIcon).resolveToItemWrapper()))
            .addIngredient('o', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setContent(node.outputItems.map(::DisplayItem))
            .build()
        return CatalogItemNodeGui(settings.title, gui)
    }
}

// --- Helper Item factories ---

private fun HintItem(settings: BasicMenuSettings): Item =
    Item.builder()
        .setItemProvider { _ ->
            settings.getIcon("hint").resolveToItemWrapper()
        }
        .build()

private fun BackgroundItem(settings: BasicMenuSettings): Item =
    Item.builder()
        .setItemProvider { _ ->
            settings.getIcon("background").resolveToItemWrapper()
        }.build()

private fun CookingInfoItem(settings: BasicMenuSettings, cookingTime: Int, exp: Float): Item =
    Item.builder()
        .setItemProvider { _ ->
            settings.getIcon("cooking_info").resolveToItemWrapper {
                standard {
                    component("cooking_time", Component.text(DecimalFormat("#.#").format(cookingTime / 20.0)))
                    component("exp", Component.text(DecimalFormat("#.##").format(exp)))
                }
            }
        }
        .build()

private fun FuelItem(settings: BasicMenuSettings): Item =
    Item.builder()
        .setItemProvider { _ ->
            settings.getIcon("fuel").resolveToItemWrapper()
        }
        .build()

private fun PrevPageItem(settings: BasicMenuSettings): BoundItem.Builder<PagedGui<*>> =
    BoundItem.pagedBuilder()
        .setItemProvider { _, gui ->
            if (gui.page <= 0)
                settings.getIcon("background").resolveToItemWrapper()
            else
                settings.getIcon("prev_page").resolveToItemWrapper {
                    standard {
                        component("current_page", Component.text(gui.page + 1))
                        component("total_page", Component.text(gui.pageCount))
                    }
                }
        }
        .addClickHandler { _, gui, _ ->
            gui.page -= 1
        }

private fun NextPageItem(settings: BasicMenuSettings): BoundItem.Builder<PagedGui<*>> =
    BoundItem.pagedBuilder()
        .setItemProvider { _, gui ->
            if (gui.page >= gui.pageCount - 1) settings.getIcon("background").resolveToItemWrapper()
            else settings.getIcon("next_page").resolveToItemWrapper {
                standard {
                    component("current_page", Component.text(gui.page + 1))
                    component("total_page", Component.text(gui.pageCount))
                }
            }
        }
        .addClickHandler { _, gui, _ ->
            gui.page += 1
        }

// --- Display Item factories ---

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

@Suppress("FunctionName")
private fun DisplayItem(items: List<ItemRef>, amount: Int = 1): Item {
    if (items.isEmpty())
        return Item.simple(ItemStack.empty())
    return if (items.size == 1) {
        DisplayItem(items.first(), amount)
    } else {
        MultiDisplayItem(items, amount)
    }
}

@Suppress("FunctionName")
private fun DisplayItem(item: ItemRef, amount: Int = 1): Item {
    return Item.builder()
        .setItemProvider { _ ->
            ItemWrapper(item.createItemStack(amount))
        }
        .addClickHandler { _, click ->
            handleClickCommons(click.clickType, click.player, item)
        }
        .build()
}

/**
 * Multi-item cycling display. Uses InvUI's built-in [Item.Builder.updatePeriodically]
 * to cycle through items. The periodic update is lifecycle-managed by InvUI and
 * automatically stops when the item is removed from all windows.
 */
private fun MultiDisplayItem(items: List<ItemRef>, amount: Int): Item {
    val period = 20
    return Item.builder()
        .setItemProvider { _ ->
            val state = (Bukkit.getCurrentTick() / period) % items.size
            ItemWrapper(items[state].createItemStack(amount))
        }
        .addClickHandler { _, click ->
            val state = (Bukkit.getCurrentTick() / period) % items.size
            handleClickCommons(click.clickType, click.player, items[state])
        }
        .updatePeriodically(period)
        .build()
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