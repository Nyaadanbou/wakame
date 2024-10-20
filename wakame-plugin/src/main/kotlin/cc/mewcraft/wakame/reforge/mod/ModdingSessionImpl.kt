package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.common.TemporaryIcons
import cc.mewcraft.wakame.util.*
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.*
import org.slf4j.Logger
import team.unnamed.mocha.runtime.MochaFunction
import java.util.stream.Stream
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 定制*核孔*的过程, 封装了一次定制所需要的所有状态.
 */
internal class SimpleModdingSession(
    override val table: ModdingTable,
    override val viewer: Player,
) : ModdingSession, KoinComponent {
    companion object {
        const val PREFIX = ReforgeLoggerPrefix.MOD
    }

    val logger: Logger = get()

    fun info(message: String) {
        logger.info("$PREFIX $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        logger.error("$PREFIX $message", throwable)
    }

    // 初始为 null
    override var inputItem: ItemStack? by InputItemDelegate(null)

    // 初始为 null
    override var sourceItem: NekoStack? by SourceItemDelegate(null)

    // 初始为 ReplaceMap.empty()
    override var replaceParams: ModdingSession.ReplaceMap by Delegates.observable(ReforgeReplaceMap.empty()) { _, old, new ->
        info("Session's replace parameters updated: $old -> $new")
    }

    // 从配置文件编译 MochaFunction
    override val totalFunction: MochaFunction = table.currencyCost.total.compile(this)

    // 初始为 ReforgeResult.empty()
    override var latestResult: ModdingSession.ReforgeResult by Delegates.observable(ReforgeResult.empty()) { _, old, new ->
        info("Session's result updated: $old -> $new")
    }

    // 初始为 false
    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            error("Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }

        info("Session's frozen status updated: $new")
        return@vetoable true
    }

    private fun executeReforge0(): ModdingSession.ReforgeResult {
        return try {
            ReforgeOperation(this)
        } catch (e: Exception) {
            error("An error occurred while executing reforge operation", e)
            ReforgeResult.failure("<red>内部错误".mini)
        }
    }

    override fun executeReforge(): ModdingSession.ReforgeResult {
        return executeReforge0().also { latestResult = it }
    }

    override fun getAllInputs(): Array<ItemStack> {
        val result = mutableListOf<ItemStack>()

        // 被定制的物品
        inputItem?.let(result::add)

        // 定制所需的耗材
        for (replace in replaceParams.values) {
            replace.latestResult.ingredient?.itemStack?.let(result::add)
        }

        return result.toTypedArray()
    }

    override fun getFinalOutputs(): Array<ItemStack> {
        val result = mutableListOf<ItemStack>()

        if (latestResult.successful) {
            // 定制后的物品
            val itemStack = latestResult.outputItem?.itemStack
            if (itemStack == null) {
                error("Output item is null, but the player is trying to take it. This is a bug!")
                return emptyArray()
            }
            result.add(itemStack)

            // 未使用的耗材
            for (replace in replaceParams.values) {
                val replaceResult = replace.latestResult
                val ingredient = replaceResult.ingredient?.itemStack
                if (ingredient != null && !replaceResult.applicable) {
                    // 无法应用的物品不会被消耗, 因此应该算到 *未使用* 的输入当中
                    result.add(ingredient)
                }
            }

        } else {
            // 如果定制的结果是未成功, 那么输入的物品就不应该被消耗, 因此应该算到 *未使用* 的输入当中
            inputItem?.let(result::add)

            // 所有输入的耗材, 无论是否能应用
            for (replace in replaceParams.values) {
                replace.latestResult.ingredient?.itemStack?.let(result::add)
            }
        }

        return result.toTypedArray()
    }

    override fun reset() {
        inputItem = null // the InputItemDelegate should handle everything
    }

    override fun getSourceItemLevel(): Int {
        return sourceItem?.components?.get(ItemComponentTypes.LEVEL)?.level ?: 0
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
            .sumOf { it.total.evaluate() }
        return total
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("table", table),
        ExaminableProperty.of("viewer", viewer.name),
        ExaminableProperty.of("sourceItem", sourceItem),
    )

    override fun toString(): String = toSimpleString()

    private inner class InputItemDelegate(
        private var _value: ItemStack?,
    ) : ReadWriteProperty<Any?, ItemStack?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): ItemStack? {
            return _value?.clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ItemStack?) {
            val old = _value
            _value = value?.clone()

            if (_value == null) {
                // 传入的是 null, 重置所有状态
                replaceParams = ReforgeReplaceMap.empty()
                latestResult = ReforgeResult.empty()
                return
            }

            info("Session's input item updated: ${old?.type} -> ${value?.type}")

            val sourceItem0 = _value?.tryNekoStack ?: return
            val sourceItemCells = sourceItem0.components.get(ItemComponentTypes.CELLS)
            val sourceItemRule = sourceItem0.let { table.itemRuleMap[it.id] }

            if (
                sourceItemCells == null || // 源物品没有核孔组件
                sourceItemRule == null // 源物品没有定制规则
            ) {
                sourceItem = null
                replaceParams = ReforgeReplaceMap.empty()
                executeReforge()
                return
            }

            sourceItem = sourceItem0
            replaceParams = createReplaceParameters(sourceItemCells, sourceItemRule)
            executeReforge()
        }

        private fun createReplaceParameters(
            sourceCells: ItemCells,
            sourceItemRule: ModdingTable.ItemRule,
        ): ModdingSession.ReplaceMap {
            val map = mutableMapOf<String, ModdingSession.Replace>()
            for ((id, cell) in sourceCells) {
                val cellRule = sourceItemRule.cellRules[id]
                if (cellRule != null) {
                    map[id] = ReforgeReplace.changeable(this@SimpleModdingSession, cell, cellRule)
                } else {
                    map[id] = ReforgeReplace.unchangeable(this@SimpleModdingSession, cell)
                }
            }

            return ReforgeReplaceMap.simple(map)
        }
    }

    private inner class SourceItemDelegate(
        private var _value: NekoStack?,
    ) : ReadWriteProperty<Any?, NekoStack?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): NekoStack? {
            return _value?.clone()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: NekoStack?) {
            val old = _value
            _value = value?.clone()

            info("Session's source item updated: ${old?.id} -> ${value?.id}")
        }
    }
}

internal object ReforgeResult {
    /**
     * 空的结果. 当要定制的物品不存在时, 用这个.
     */
    fun empty(): ModdingSession.ReforgeResult {
        return Empty()
    }

    /**
     * 失败的结果. 当存在要定制的物品, 但由于某种原因无法定制时, 用这个.
     */
    fun failure(description: List<Component>): ModdingSession.ReforgeResult {
        return Failure(description)
    }

    /**
     * 失败的结果. 当存在要定制的物品, 但由于某种原因无法定制时, 用这个.
     */
    fun failure(description: Component): ModdingSession.ReforgeResult {
        return failure(listOf(description))
    }

    /**
     * 成功的结果. 当成功定制物品时, 用这个.
     */
    fun success(
        outputItem: NekoStack,
        cost: ModdingSession.Cost,
    ): ModdingSession.ReforgeResult {
        return Success(outputItem, listOf("<gray>定制准备就绪!".mini), cost)
    }

    private abstract class Base : ModdingSession.ReforgeResult {
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
        override val cost: ModdingSession.Cost = ReforgeCost.empty()
    }

    private class Failure(
        description: List<Component>,
    ) : Base() {
        override val isEmpty: Boolean = false
        override val successful: Boolean = false
        override val description: List<Component> = description
        override val outputItem: NekoStack? = null
        override val cost: ModdingSession.Cost = ReforgeCost.empty()
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

internal object ReforgeCost {
    fun empty(): ModdingSession.Cost {
        return Empty()
    }

    /**
     * @param currencyAmount 要消耗的默认货币数量
     */
    fun simple(
        currencyAmount: Double,
    ): ModdingSession.Cost {
        return Simple(currencyAmount)
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

    private class Simple(
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

private object ReforgeReplace {
    /**
     * 封装一个不可修改的核孔.
     */
    fun unchangeable(session: SimpleModdingSession, cell: Cell): ModdingSession.Replace {
        return Unchangeable(session, cell)
    }

    /**
     * 封装一个可以修改的核孔.
     */
    fun changeable(session: SimpleModdingSession, cell: Cell, rule: ModdingTable.CellRule): ModdingSession.Replace {
        return Changeable(session, cell, rule)
    }

    private const val PREFIX = ReforgeLoggerPrefix.MOD
    private val ZERO_MOCHA_FUNCTION: MochaFunction = MochaFunction { .0 }

    private class Unchangeable(
        override val session: SimpleModdingSession,
        override val cell: Cell,
    ) : ModdingSession.Replace, KoinComponent {
        private val logger: Logger by inject()

        override val id
            get() = cell.getId()
        override val rule
            get() = ModdingTable.CellRule.empty()
        override val display = ItemStack(TemporaryIcons.get(id.hashCode())).apply {
            val unchangeable = "<red>(不可修改)"
            editMeta { meta ->
                // TODO 使用新的渲染器生成文本
                val name = "${cell.getCore().id.asString()} $unchangeable".mini
                meta.itemName(name)
                meta.hideAllFlags()
            }
        }
        override val total
            get() = ZERO_MOCHA_FUNCTION // 不可修改的核孔不需要花费 (?)
        override val changeable: Boolean
            get() = false
        override var latestResult by Delegates.observable(ReforgeReplaceResult.empty()) { _, old, new ->
            session.info("Replace (unchangeable) result updated: $old -> $new")
        }
        override val hasInput: Boolean
            get() = latestResult.ingredient != null

        private fun executeReplace0(ingredient: NekoStack?): ModdingSession.Replace.Result {
            if (ingredient == null)
                return ReforgeReplaceResult.empty()
            return ReforgeReplaceResult.failure(ingredient, "<gray>源物品的核孔不可修改".mini)
        }

        override fun executeReplace(ingredient: NekoStack?): ModdingSession.Replace.Result {
            val result = executeReplace0(ingredient)
            latestResult = result
            return result
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
        override val session: SimpleModdingSession,
        override val cell: Cell,
        override val rule: ModdingTable.CellRule,
    ) : ModdingSession.Replace, KoinComponent {
        private val logger: Logger by inject()

        override val id: String
            get() = cell.getId()

        override val display: ItemStack = ItemStack(TemporaryIcons.get(id.hashCode())).apply {
            editMeta { meta ->
                // TODO 使用新的渲染器生成文本
                val name = cell.getId().mini
                val lore = listOf(cell.getCore().id.asString().mini)
                meta.itemName(name)
                meta.lore(lore)
                meta.hideAllFlags()
            }
        }

        override val total: MochaFunction = rule.currencyCost.total.compile(session, this)

        override val changeable: Boolean
            get() = true

        override var latestResult: ModdingSession.Replace.Result by Delegates.observable(ReforgeReplaceResult.empty()) { _, old, new ->
            session.info("Replace (changeable) result updated: $old -> $new")
        }

        override val hasInput: Boolean
            get() = latestResult.ingredient != null

        override fun executeReplace(ingredient: NekoStack?): ModdingSession.Replace.Result {
            return executeReplace0(ingredient).also { latestResult = it }
        }

        private fun executeReplace0(ingredient: NekoStack?): ModdingSession.Replace.Result {
            // 如果耗材为空, 则返回空结果
            if (ingredient == null) {
                return ReforgeReplaceResult.empty()
            }

            // 如果源物品为空, 则返回内部错误
            val sourceItem = session.sourceItem ?: run {
                session.error("Source item is null, but an item is being replaced. This is a bug!")
                return ReforgeReplaceResult.failure(ingredient, "<red>内部错误".mini)
            }

            // TODO 检查权限

            // 获取耗材中的便携核心
            val portableCore = ingredient.components.get(ItemComponentTypes.PORTABLE_CORE) ?: run {
                return ReforgeReplaceResult.failure(ingredient, "<gray>这个物品不是便携核心".mini)
            }

            // 获取源物品上的核孔
            val sourceCells = sourceItem.components.get(ItemComponentTypes.CELLS) ?: run {
                session.error("Source item has no cells, but an item is being replaced. This is a bug!")
                return ReforgeReplaceResult.failure(ingredient, "<red>内部错误".mini)
            }

            // 源物品的核孔上 必须没有与便携核心相似的核心
            val sourceCellsExcludingThis = sourceCells.filter2 { it.getId() != cell.getId() }
            if (sourceCellsExcludingThis.containSimilarCore(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure(ingredient, "<gray>源物品上存在相似的便携核心".mini)
            }

            if (session.replaceParams.containsCoreSimilarTo(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure(ingredient, "<gray>输入之中存在相似的便携核心".mini)
            }

            // 便携式核心的类型 必须符合定制规则
            if (!rule.acceptableCores.test(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure(ingredient, "<gray>源物品的核孔不接受这种便携核心".mini)
            }

            // 便携式核心上面的所有元素 必须全部出现在被定制物品上
            if (rule.requireElementMatch) {
                val elementsOnSource = sourceItem.components.get(ItemComponentTypes.ELEMENTS)?.elements ?: emptySet()
                // 这里要求耗材上只有一种元素, 并且元素是存在核心里面的
                val elementOnIngredient = (portableCore.wrapped as? AttributeCore)?.attribute?.element
                if (elementOnIngredient != null && elementOnIngredient !in elementsOnSource) {
                    return ReforgeReplaceResult.failure(ingredient, "<gray>便携核心的元素跟源物品的不相融".mini)
                }
            }

            // 被定制物品上储存的历史定制次数 必须小于等于定制规则
            val modCount = sourceItem.components.get(ItemComponentTypes.CELLS)?.get(id)?.getReforgeHistory()?.modCount ?: Int.MAX_VALUE
            if (modCount >= rule.modLimit) {
                return ReforgeReplaceResult.failure(ingredient, "<gray>源物品的核孔已经历经无数雕琢".mini)
            }

            // 全部检查通过!
            return ReforgeReplaceResult.success(ingredient, "<green>便携核心准备就绪".mini)
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

private object ReforgeReplaceResult {
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

private object ReforgeReplaceMap {
    /**
     * 返回一个 不可变的 空的 [ReforgeReplaceMap].
     *
     * 当没有可以定制的核孔时, 使用这个.
     */
    fun empty(): ModdingSession.ReplaceMap {
        return Empty
    }

    /**
     * 返回一个 新的 可变的 [ReforgeReplaceMap].
     *
     * 当存在可以定制的核孔时, 使用这个.
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
        override fun containsCoreSimilarTo(core: Core): Boolean = false
        override fun getAllInputs(): Array<ItemStack> = emptyArray()
        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Replace>> = emptyList<Map.Entry<String, ModdingSession.Replace>>().iterator()
        override fun toString(): String = toSimpleString()
    }

    private class Simple(
        data: Map<String, ModdingSession.Replace>,
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

        override fun containsCoreSimilarTo(core: Core): Boolean {
            return data.values.any { replace ->
                replace.latestResult.ingredient
                    ?.components
                    ?.get(ItemComponentTypes.PORTABLE_CORE)
                    ?.wrapped
                    ?.similarTo(core) == true
            }
        }

        override fun getAllInputs(): Array<ItemStack> {
            return data.values.mapNotNull { it.latestResult.ingredient?.itemStack }.toTypedArray()
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
