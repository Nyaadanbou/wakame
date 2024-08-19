package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.common.TemporaryIcons
import cc.mewcraft.wakame.util.hideAllFlags
import cc.mewcraft.wakame.util.hideTooltip
import cc.mewcraft.wakame.util.plain
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import team.unnamed.mocha.runtime.MochaFunction
import java.util.stream.Stream
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 定制词条栏*核心*的过程, 封装了一次定制所需要的所有状态.
 */
internal class SimpleModdingSession(
    override val table: ModdingTable,
    override val viewer: Player,
    sourceItem: NekoStack? = null,
) : ModdingSession, KoinComponent {
    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.MOD
    }

    private val logger: Logger by inject()

    override var sourceItem: NekoStack? by SourceItemDelegate(sourceItem)

    override var isSourceItemLegit: Boolean by Delegates.observable(false) { _, old, new ->
        logger.info("$PREFIX Session's source item legitimacy updated: $old -> $new")
    }

    override var replaceParams: ModdingSession.ReplaceMap by Delegates.observable(ReplaceMap.empty()) { _, old, new ->
        logger.info("$PREFIX Session's replace parameters updated: $old -> $new")
    }

    override val totalFunction: MochaFunction = table.currencyCost.total.compile(this)

    override var latestResult: ModdingSession.Result by Delegates.observable(Result.empty()) { _, old, new ->
        logger.info("$PREFIX Session's result updated: $old -> $new")
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("$PREFIX Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }

        logger.info("$PREFIX Session's frozen status updated: $new")
        return@vetoable true
    }

    private fun executeReforge0(): ModdingSession.Result {
        if (frozen) {
            logger.info("$PREFIX Trying to reforge in a frozen session. This is a bug!")
            return Result.empty()
        }

        // 如果源物品不存在, 则返回空
        val output = sourceItem ?: return Result.empty()

        if (!isSourceItemLegit) {
            logger.info("$PREFIX Skipped reforge as source item is not legit")
            return Result.failure("<gray>不可定制".mini)
        }

        if (replaceParams.all { (_, repl) -> !repl.changed }) {
            logger.info("$PREFIX Skipped reforge as all replaces are not applicable")
            return Result.failure("<gray>没有任何修改".mini)
        }

        // 把经过修改的词条栏筛选出来
        val changedReplaceParams = replaceParams.filter { (_, repl) -> repl.changed }

        // 检查经过修改的词条栏中是否存在无效的耗材
        if (changedReplaceParams.any { (_, repl) -> !repl.latestResult.applicable }) {
            logger.info("$PREFIX Skipped reforge as some replaces are not applicable")
            return Result.failure("<gray>存在无效的耗材".mini)
        }

        // 如果源物品不合法, 则返回失败
        val cellBuilder = output.components.get(ItemComponentTypes.CELLS)?.builder() ?: run {
            logger.info("$PREFIX No cells found in source item")
            return Result.failure("<gray>源物品没有词条栏".mini)
        }

        for ((id, replace) in changedReplaceParams) {
            val result = replace.latestResult
            if (!result.applicable) {
                logger.info("$PREFIX Skipped replace '$id' as it's is not applicable")
                continue
            }

            val ingredient = result.ingredient!!
            val portableCore = ingredient.components.get(ItemComponentTypes.PORTABLE_CORE)!!
            val wrappedCore = portableCore.wrapped

            cellBuilder.modify(id) { cell -> cell.setCore(wrappedCore) }
        }

        // 把修改后的词条栏应用到输出物品山
        output.components.set(ItemComponentTypes.CELLS, cellBuilder.build())

        // 计算需要消耗的货币数量
        val cost = Cost.normal(totalFunction.evaluate())

        return Result.success(
            outputItem = output,
            description = "<green>定制准备就绪".mini,
            cost = cost
        )
    }

    override fun executeReforge(): ModdingSession.Result {
        val result = executeReforge0()
        latestResult = result
        return result
    }

    override fun getInapplicablePlayerInputs(): Collection<ItemStack> {
        val itemsToReturn = buildList {
            if (!isSourceItemLegit) {
                sourceItem?.let { add(it.itemStack) }
            }

            for (replace in replaceParams.values) {
                val result = replace.latestResult
                val ingredient = result.ingredient?.itemStack
                if (ingredient != null && (!result.applicable /* 无法应用的物品不会被消耗, 因此应该被返回 */)) {
                    add(ingredient)
                }
            }
        }

        return itemsToReturn
    }

    override fun getAllPlayerInputs(): Collection<ItemStack> {
        val itemsToReturn = buildList {

            // 收集被定制的物品
            sourceItem?.let { add(it.itemStack) }

            // 收集用于定制的所有耗材
            for (replace in replaceParams.values) {
                val ingredient = replace.latestResult.ingredient?.itemStack
                if (ingredient != null) {
                    add(ingredient)
                }
            }
        }

        return itemsToReturn
    }

    override fun reset() {
        sourceItem = null
        replaceParams = ReplaceMap.empty()
        latestResult = Result.empty()
    }

    override fun getSourceItemLevel(): Int {
        return sourceItem?.components?.get(ItemComponentTypes.LEVEL)?.level?.toInt() ?: 0
    }

    override fun getSourceItemRarityNumber(): Double {
        return sourceItem?.components?.get(ItemComponentTypes.RARITY)?.rarity?.let { table.rarityNumberMapping.get(it.key) } ?: .0
    }

    override fun getSourceItemTotalCellCount(): Int {
        return sourceItem?.components?.get(ItemComponentTypes.CELLS)?.size ?: 0
    }

    override fun getSourceItemChangeableCellCount(): Int {
        return replaceParams.size
    }

    override fun getSourceItemChangedCellCount(): Int {
        return replaceParams.count { (_, repl) -> repl.latestResult.applicable }
    }

    override fun getSourceItemChangedCellCost(): Double {
        val total = replaceParams
            .values
            .filter { it.latestResult.applicable }
            .sumOf { it.totalFunction.evaluate() }
        return total
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("table", table),
        ExaminableProperty.of("viewer", viewer.name),
        ExaminableProperty.of("sourceItem", sourceItem),
    )

    override fun toString(): String = toSimpleString()

    private inner class SourceItemDelegate(
        private var _value: NekoStack?
    ) : ReadWriteProperty<Any?, NekoStack?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): NekoStack? {
            return _value?.clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: NekoStack?) {
            val clone = value?.clone()
            _value = clone

            logger.info("$PREFIX Session's source item updated: ${_value?.key} -> ${value?.key}")

            val sourceCells = clone?.components?.get(ItemComponentTypes.CELLS)
            val sourceItemRule = clone?.let { table.itemRules[it.key] }

            if (
                clone == null // 源物品不存在
                || sourceCells == null // 源物品没有词条栏
                || sourceItemRule == null // 源物品没有对应的定制规则
            ) {
                isSourceItemLegit = false
                replaceParams = ReplaceMap.empty()
                executeReforge()
                return
            }

            isSourceItemLegit = true
            replaceParams = createNonEmptyReplaceParameters(clone, sourceCells, sourceItemRule)
            executeReforge()
        }

        private fun createNonEmptyReplaceParameters(
            sourceItem: NekoStack,
            sourceCells: ItemCells,
            sourceItemRule: ModdingTable.ItemRule,
        ): ModdingSession.ReplaceMap {
            val map = mutableMapOf<String, ModdingSession.Replace>()
            for ((id, cell) in sourceCells) {
                val cellRule = sourceItemRule.cellRules[id]
                if (cellRule != null) {
                    map[id] = Replace.changeable(this@SimpleModdingSession, cell, cellRule)
                } else {
                    map[id] = Replace.unchangeable(this@SimpleModdingSession, cell)
                }
            }

            return ReplaceMap.simple(map)
        }
    }

    object Result {
        /**
         * 空的结果. 当要定制的物品不存在时, 用这个.
         */
        fun empty(): ModdingSession.Result {
            return Empty()
        }

        /**
         * 失败的结果. 当存在要定制的物品, 但由于某种原因无法定制时, 用这个.
         */
        fun failure(description: List<Component>): ModdingSession.Result {
            return Failure(description)
        }

        /**
         * 失败的结果. 当存在要定制的物品, 但由于某种原因无法定制时, 用这个.
         */
        fun failure(description: Component): ModdingSession.Result {
            return failure(listOf(description))
        }

        /**
         * 成功的结果. 当成功定制物品时, 用这个.
         */
        fun success(
            outputItem: NekoStack,
            description: List<Component>,
            cost: ModdingSession.Cost
        ): ModdingSession.Result {
            return Success(outputItem, description, cost)
        }

        /**
         * 成功的结果. 当成功定制物品时, 用这个.
         */
        fun success(
            outputItem: NekoStack,
            description: Component,
            cost: ModdingSession.Cost
        ): ModdingSession.Result {
            return success(outputItem, listOf(description), cost)
        }

        private abstract class Base : ModdingSession.Result {
            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("successful", successful),
                ExaminableProperty.of("description", description.plain),
                ExaminableProperty.of("outputItem", outputItem),
                ExaminableProperty.of("cost", cost),
            )

            override fun toString(): String = toSimpleString()
        }

        private class Empty : Base() {
            override val isEmpty: Boolean = true
            override val successful: Boolean = false
            override val description: List<Component> = listOf(
                "<gray>没有输出结果".mini
            )
            override val outputItem: NekoStack? = null
            override val cost: ModdingSession.Cost = Cost.empty()
        }

        private class Failure(
            description: List<Component>,
        ) : Base() {
            override val isEmpty: Boolean = false
            override val successful: Boolean = false
            override val description: List<Component> = description
            override val outputItem: NekoStack? = null
            override val cost: ModdingSession.Cost = Cost.empty()
        }

        private class Success(
            outputItem: NekoStack,
            description: List<Component>,
            cost: ModdingSession.Cost,
        ) : Base() {
            override val isEmpty: Boolean = false
            override val successful: Boolean = true
            override val description: List<Component> = description
            override val outputItem: NekoStack by NekoStackDelegates.copyOnRead(outputItem)
            override val cost: ModdingSession.Cost = cost
        }
    }

    object Cost {
        fun empty(): ModdingSession.Cost {
            return Empty()
        }

        /**
         * @param defaultCurrencyAmount 要消耗的默认货币数量
         */
        fun normal(
            defaultCurrencyAmount: Double
        ): ModdingSession.Cost {
            return Normal(defaultCurrencyAmount)
        }

        private class Empty : ModdingSession.Cost {
            override fun take(viewer: Player) {
                // do nothing
            }

            override fun test(viewer: Player): Boolean {
                return true
            }

            override val description: List<Component> = listOf(
                "<gray>无资源消耗".mini
            )

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("description", description.plain)
            )

            override fun toString(): String = toSimpleString()
        }

        private class Normal(
            val currencyAmount: Double,
        ) : ModdingSession.Cost {
            override fun take(viewer: Player) {
                // TODO 实现 take, test, description
            }

            override fun test(viewer: Player): Boolean {
                return true
            }

            override val description: List<Component> = listOf(
                "<gray>金币: <yellow>${currencyAmount}".mini
            )

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("description", description.plain),
                ExaminableProperty.of("currencyAmount", currencyAmount)
            )

            override fun toString(): String = toSimpleString()
        }
    }

    object Replace {
        /**
         * 封装一个不可修改的词条栏.
         */
        fun unchangeable(session: ModdingSession, cell: Cell): ModdingSession.Replace {
            return Unchangeable(session, cell)
        }

        /**
         * 封装一个可以修改的词条栏.
         */
        fun changeable(session: ModdingSession, cell: Cell, rule: ModdingTable.CellRule): ModdingSession.Replace {
            return Changeable(session, cell, rule)
        }

        private const val PREFIX = ReforgeLoggerPrefix.MOD

        private val zeroMochaFunction: MochaFunction = MochaFunction { .0 }

        private class Unchangeable(
            override val session: ModdingSession,
            override val cell: Cell,
        ) : ModdingSession.Replace, KoinComponent {
            override val id = cell.getId()
            override val rule = ModdingTable.CellRule.empty()
            override val display = ItemStack(Material.BARRIER).hideTooltip(true)
            override val totalFunction = zeroMochaFunction // 不可修改的词条栏不需要花费 (?)
            override val changed: Boolean = false
            override var latestResult = ReplaceResult.empty()

            override fun executeReplace(ingredient: NekoStack?): ModdingSession.Replace.Result {
                if (ingredient == null) {
                    return ReplaceResult.empty()
                }

                return ReplaceResult.failure(ingredient, "<gray>源物品的这个词条栏不可修改".mini)
            }

            override fun getIngredientLevel(): Int = 0
            override fun getIngredientRarityNumber(): Double = .0

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("id", id),
                ExaminableProperty.of("latestResult", latestResult),
            )

            override fun toString(): String = toSimpleString()
        }

        private class Changeable(
            override val session: ModdingSession,
            override val cell: Cell,
            override val rule: ModdingTable.CellRule,
        ) : ModdingSession.Replace, KoinComponent {
            private val logger: Logger by inject()

            override val id: String = cell.getId()

            override val display: ItemStack = ItemStack(TemporaryIcons.get(id.hashCode())).apply {
                editMeta { meta ->
                    val name = cell.provideTooltipName().content
                    val lore = cell.provideTooltipLore().content
                    meta.itemName(name)
                    meta.lore(lore)
                    meta.hideAllFlags()
                }
            }

            override val totalFunction: MochaFunction = rule.currencyCost.total.compile(session, this)

            override val changed: Boolean
                // 当耗材不为空时, 代表这个词条栏
                // 已经被修改过, 不管成功与否
                get() = latestResult.ingredient != null

            override var latestResult: ModdingSession.Replace.Result by Delegates.observable(ReplaceResult.empty()) { _, old, new ->
                logger.info("$PREFIX Replace result updated: $old -> $new")
            }

            private fun executeReplace0(ingredient: NekoStack?): ModdingSession.Replace.Result {
                // 如果耗材为空, 则返回空结果
                if (ingredient == null) {
                    return ReplaceResult.empty()
                }

                // 如果源物品为空, 则返回内部错误
                val sourceItem = session.sourceItem ?: run {
                    logger.error("$PREFIX Source item is null, but an item is being replaced. This is a bug!")
                    return ReplaceResult.failure(ingredient, "<red>内部错误".mini)
                }

                // TODO 检查权限

                // 获取耗材中的便携核心
                val portableCore = ingredient.components.get(ItemComponentTypes.PORTABLE_CORE) ?: run {
                    return ReplaceResult.failure(ingredient, "<gray>这个物品没有便携核心".mini)
                }

                // 获取源物品上的词条栏
                val sourceCells = sourceItem.components.get(ItemComponentTypes.CELLS) ?: run {
                    logger.error("$PREFIX Source item has no cells, but an item is being replaced. This is a bug!")
                    return ReplaceResult.failure(ingredient, "<red>内部错误".mini)
                }

                // 源物品的词条栏上必须没有与便携核心相似的核心
                val sourceCellsExcludingThis = sourceCells.filterx { it.getId() != cell.getId() }
                if (sourceCellsExcludingThis.hasSimilarCore(portableCore.wrapped)) {
                    return ReplaceResult.failure(ingredient, "<gray>源物品上存在相似的便携核心".mini)
                }

                // 便携式核心 必须符合定制规则
                if (!rule.acceptedCores.test(portableCore.wrapped)) {
                    return ReplaceResult.failure(ingredient, "<gray>源物品的词条栏不接受这种便携核心".mini)
                }

                // 便携式核心上面的所有元素 必须全部出现在被定制物品上
                if (rule.requireElementMatch) {
                    val elementsOnSource = sourceItem.components.get(ItemComponentTypes.ELEMENTS)?.elements ?: emptySet()
                    val elementsOnIngredient = ingredient.components.get(ItemComponentTypes.ELEMENTS)?.elements ?: emptySet()
                    if (!elementsOnIngredient.containsAll(elementsOnSource)) {
                        return ReplaceResult.failure(ingredient, "<gray>便携核心的元素跟源物品的不相融".mini)
                    }
                }

                // 被定制物品上储存的历史定制次数 必须小于等于定制规则
                val modCount = ingredient.components.get(ItemComponentTypes.CELLS)?.get(id)?.getReforgeHistory()?.modCount ?: Int.MAX_VALUE
                if (modCount > rule.modLimit) {
                    return ReplaceResult.failure(ingredient, "<gray>源物品的这个词条栏已经历经无数雕琢".mini)
                }

                // 全部检查通过!
                return ReplaceResult.success(ingredient, "<green>便携核心准备就绪".mini)
            }

            override fun executeReplace(ingredient: NekoStack?): ModdingSession.Replace.Result {
                val result = executeReplace0(ingredient)
                latestResult = result
                return result
            }

            override fun getIngredientLevel(): Int {
                return latestResult.ingredient?.components?.get(ItemComponentTypes.LEVEL)?.level?.toInt() ?: 0
            }

            override fun getIngredientRarityNumber(): Double {
                return latestResult.ingredient?.components?.get(ItemComponentTypes.RARITY)?.rarity?.let { session.table.rarityNumberMapping.get(it.key) } ?: .0
            }

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("id", id),
                ExaminableProperty.of("latestResult", latestResult),
            )

            override fun toString(): String = toSimpleString()
        }
    }

    object ReplaceResult {
        /**
         * 空的结果. 当没有耗材输入时, 用这个.
         */
        fun empty(): ModdingSession.Replace.Result {
            return Empty()
        }

        fun failure(ingredient: NekoStack?, description: List<Component>): ModdingSession.Replace.Result {
            return Simple(ingredient, false, description)
        }

        fun failure(ingredient: NekoStack?, description: Component): ModdingSession.Replace.Result {
            return failure(ingredient, listOf(description))
        }

        fun success(ingredient: NekoStack?, description: List<Component>): ModdingSession.Replace.Result {
            return Simple(ingredient, true, description)
        }

        fun success(ingredient: NekoStack?, description: Component): ModdingSession.Replace.Result {
            return success(ingredient, listOf(description))
        }

        private abstract class Base : ModdingSession.Replace.Result {
            override fun getPortableCore(): PortableCore? {
                return ingredient?.components?.get(ItemComponentTypes.PORTABLE_CORE)
            }

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("ingredient", ingredient),
                ExaminableProperty.of("applicable", applicable),
                ExaminableProperty.of("description", description.plain),
            )

            override fun toString(): String = toSimpleString()
        }

        private class Empty : Base() {
            override val ingredient: NekoStack? = null
            override val applicable: Boolean = false // 空气无法参与定制, 需要额外逻辑判断
            override val description: List<Component> = listOf("<gray>没有耗材输入".mini)
        }

        private class Simple(
            ingredient: NekoStack?,
            override val applicable: Boolean,
            override val description: List<Component>,
        ) : Base() {
            override val ingredient: NekoStack? by NekoStackDelegates.nullableCopyOnRead(ingredient)
        }
    }

    object ReplaceMap {
        /**
         * 返回一个 不可变的 空的 [ReplaceMap].
         *
         * 当没有可以定制的词条栏时, 使用这个.
         */
        fun empty(): ModdingSession.ReplaceMap {
            return Empty
        }

        /**
         * 返回一个 新的 可变的 [ReplaceMap].
         *
         * 当存在可以定制的词条栏时, 使用这个.
         */
        fun simple(data: Map<String, ModdingSession.Replace> = HashMap()): ModdingSession.ReplaceMap {
            return Simple(data)
        }

        private object Empty : ModdingSession.ReplaceMap {
            override val size: Int = 0
            override val keys: Set<String> = emptySet()
            override val values: Collection<ModdingSession.Replace> = emptyList()

            override fun get(id: String): ModdingSession.Replace? = null
            override fun contains(id: String): Boolean = false
            override fun getPlayerInputs(): Collection<ItemStack> = emptyList()
            override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Replace>> = emptyList<Map.Entry<String, ModdingSession.Replace>>().iterator()
            override fun toString(): String = toSimpleString()
        }

        private class Simple(
            data: Map<String, ModdingSession.Replace>
        ) : ModdingSession.ReplaceMap {
            val data: HashMap<String, ModdingSession.Replace> = HashMap(data) // explicit copy

            override val size: Int
                get() = data.size
            override val keys: Set<String>
                get() = data.keys
            override val values: Collection<ModdingSession.Replace>
                get() = data.values

            override fun get(id: String): ModdingSession.Replace? {
                return data[id]
            }

            override fun contains(id: String): Boolean {
                return data.containsKey(id)
            }

            override fun getPlayerInputs(): List<ItemStack> {
                return data.values.mapNotNull { it.latestResult.ingredient?.unsafe?.handle }
            }

            override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Replace>> {
                return data.iterator()
            }

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("size", size),
                ExaminableProperty.of("data", data),
            )

            override fun toString(): String {
                return toSimpleString()
            }
        }
    }
}
