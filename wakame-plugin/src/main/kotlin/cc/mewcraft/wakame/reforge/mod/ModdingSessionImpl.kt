package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.attribute.bundle.element
import cc.mewcraft.wakame.integration.economy.EconomyManager
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.cells
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.elements
import cc.mewcraft.wakame.item.level
import cc.mewcraft.wakame.item.portableCore
import cc.mewcraft.wakame.item.rarity
import cc.mewcraft.wakame.item.reforgeHistory
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.lang.translate
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.mod.ModdingTable.CellRule
import cc.mewcraft.wakame.reforge.mod.ModdingTable.ItemRule
import cc.mewcraft.wakame.util.decorate
import cc.mewcraft.wakame.util.isEmpty
import cc.mewcraft.wakame.util.plain
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslationArgument
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import team.unnamed.mocha.runtime.MochaFunction
import java.util.stream.Stream
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
    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.MOD)

    // 初始为 null
    override var originalInput: ItemStack? by OriginalInputDelegate(null)

    // 初始为 null
    override var usableInput: NekoStack? by UsableInputDelegate(null)

    // 初始为 null
    override var itemRule: ItemRule? = null

    // 初始为 ReplaceMap.empty() 表示还没有输入
    override var replaceParams: ModdingSession.ReplaceMap by Delegates.observable(ReforgeReplaceMap.empty(this)) { _, old, new ->
        // logger.info("Session's replace parameters updated: $old -> $new")
    }

    // 从配置文件编译 MochaFunction
    override val totalFunction: MochaFunction = table.currencyCost.total.compile(this)

    // 初始为 ReforgeResult.empty() 表示还没有输入
    override var latestResult: ModdingSession.ReforgeResult by Delegates.observable(ReforgeResult.empty(viewer)) { _, old, new ->
        // logger.info("Session's result updated: $old -> $new")
    }

    // 初始为 false
    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }

        // logger.info("Session's frozen status updated: $new")
        return@vetoable true
    }

    private fun executeReforge0(): ModdingSession.ReforgeResult {
        return try {
            ReforgeOperation(this)
        } catch (e: Exception) {
            logger.error("An error occurred while executing reforge operation", e)
            ReforgeResult.failure(viewer, MessageConstants.MSG_ERR_INTERNAL_ERROR)
        }
    }

    override fun executeReforge(): ModdingSession.ReforgeResult {
        return executeReforge0().also { latestResult = it }
    }

    override fun getAllInputs(): Array<ItemStack> {
        val result = mutableListOf<ItemStack>()

        // 被定制的物品
        originalInput?.let(result::add)

        // 放入的耗材
        for (replace in replaceParams.values) {
            replace.originalInput?.let(result::add)
        }

        return result.toTypedArray()
    }

    override fun getFinalOutputs(): Array<ItemStack> {
        val result = mutableListOf<ItemStack>()

        if (latestResult.isSuccess) {
            // 定制后的物品
            val itemStack = latestResult.output?.itemStack
            if (itemStack == null) {
                logger.error("Output item is null, but the player is trying to take it. This is a bug!")
                return emptyArray()
            }
            result.add(itemStack)

            // 未使用的耗材
            for (replace in replaceParams.values) {
                val originalInput = replace.originalInput
                val replaceResult = replace.latestResult
                if (originalInput != null && !replaceResult.applicable) {
                    // 无法应用的物品不会被消耗, 因此应该算到 *未使用* 的输入当中
                    result.add(originalInput)
                }
            }

        } else {
            // 如果定制的结果是未成功, 那么输入的物品就不应该被消耗, 因此应该算到 *未使用* 的输入当中
            originalInput?.let(result::add)

            // 所有输入的耗材, 无论是否能应用
            for (replace in replaceParams.values) {
                replace.originalInput?.let(result::add)
            }
        }

        return result.toTypedArray()
    }

    override fun reset() {
        originalInput = null // the InputItemDelegate should handle everything
    }

    override fun getSourceItemLevel(): Int {
        return usableInput?.level ?: 0
    }

    override fun getSourceItemRarityNumber(): Double {
        return usableInput?.rarity?.getKeyOrThrow()?.value?.let(table.rarityNumberMapping::get) ?: .0
    }

    override fun getSourceItemTotalCellCount(): Int {
        return usableInput?.cells?.size ?: 0
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
        ExaminableProperty.of("usableInput", usableInput),
    )

    override fun toString(): String = toSimpleString()

    private inner class OriginalInputDelegate(
        private var _value: ItemStack?,
    ) : ReadWriteProperty<SimpleModdingSession, ItemStack?> {
        override fun getValue(thisRef: SimpleModdingSession, property: KProperty<*>): ItemStack? {
            return _value?.clone()
        }

        override fun setValue(thisRef: SimpleModdingSession, property: KProperty<*>, value: ItemStack?) {
            val old = _value
            _value = value?.clone()

            if (_value == null) {
                // 传入的是 null, 重置所有状态
                usableInput = null
                replaceParams = ReforgeReplaceMap.empty(thisRef)
                executeReforge()
                return
            }

            // logger.info("Session's input item updated: ${old?.type} -> ${value?.type}")

            val usableInput0 = _value?.shadowNeko(true) ?: run {
                usableInput = null
                replaceParams = ReforgeReplaceMap.empty(thisRef)
                executeReforge()
                return
            }
            val inputItemCells = usableInput0.components.get(ItemComponentTypes.CELLS)
            val inputItemRule = usableInput0.let { table.itemRuleMap[it.id] }

            if (
                inputItemCells == null || // 源物品没有核孔组件
                inputItemRule == null // 源物品没有定制规则
            ) {
                usableInput = null
                replaceParams = ReforgeReplaceMap.empty(thisRef)
                executeReforge()
                return
            }

            usableInput = usableInput0
            itemRule = inputItemRule
            replaceParams = createReplaceParameters(thisRef, inputItemCells, inputItemRule)
            executeReforge()
        }

        private fun createReplaceParameters(
            thisRef: SimpleModdingSession,
            itemCells: ItemCells,
            itemRule: ItemRule,
        ): ModdingSession.ReplaceMap {
            val cellRuleMap = itemRule.cellRuleMap
            val replaceData = sortedMapOf<String, ModdingSession.Replace>(cellRuleMap.comparator)
            for ((id, cell) in itemCells) {
                val cellRule = cellRuleMap[id]
                if (cellRule != null) {
                    replaceData[id] = ReforgeReplace.changeable(thisRef, cell, cellRule)
                } else {
                    replaceData[id] = ReforgeReplace.unchangeable(thisRef, cell)
                }
            }

            return ReforgeReplaceMap.simple(thisRef, LinkedHashMap(replaceData))
        }
    }

    private inner class UsableInputDelegate(
        private var _value: NekoStack?,
    ) : ReadWriteProperty<SimpleModdingSession, NekoStack?> {
        override fun getValue(thisRef: SimpleModdingSession, property: KProperty<*>): NekoStack? {
            return _value?.clone()
        }

        override fun setValue(thisRef: SimpleModdingSession, property: KProperty<*>, value: NekoStack?) {
            val old = _value
            _value = value?.clone()

            // logger.info("Session's usable input updated: ${old?.id} -> ${value?.id}")
        }
    }
}

internal object ReforgeResult {
    /**
     * 内部错误: 发生了无法预料的错误.
     */
    fun error(viewer: Player): ModdingSession.ReforgeResult {
        return Error(viewer)
    }

    /**
     * 空的结果: 要定制的物品不存在.
     */
    fun empty(viewer: Player): ModdingSession.ReforgeResult {
        return Empty(viewer)
    }

    /**
     * 失败的结果: 存在要定制的物品, 但由于某种原因无法定制.
     */
    fun failure(viewer: Player, description: List<ComponentLike>): ModdingSession.ReforgeResult {
        return Failure(viewer, description.translate(viewer))
    }

    /**
     * 失败的结果: 存在要定制的物品, 但由于某种原因无法定制.
     */
    fun failure(viewer: Player, description: ComponentLike): ModdingSession.ReforgeResult {
        return failure(viewer, listOf(description))
    }

    /**
     * 成功的结果: 成功定制物品.
     */
    fun success(viewer: Player, outputItem: NekoStack, cost: ModdingSession.ReforgeCost): ModdingSession.ReforgeResult {
        return Success(viewer, outputItem, listOf(MessageConstants.MSG_MODDING_RESULT_SUCCESS.translate(viewer)), cost)
    }

    private abstract class Base : ModdingSession.ReforgeResult {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("isSuccess", isSuccess),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("reforgeCost", reforgeCost),
            ExaminableProperty.of("output", output),
        )

        override fun toString(): String = toSimpleString()
    }

    private class Error(viewer: Player) : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = listOf(MessageConstants.MSG_ERR_INTERNAL_ERROR.translate(viewer))
        override val reforgeCost: ModdingSession.ReforgeCost = ReforgeCost.empty(viewer)
        override val output: NekoStack? = null
    }

    private class Empty(viewer: Player) : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = listOf(MessageConstants.MSG_MODDING_RESULT_EMPTY.translate(viewer))
        override val reforgeCost: ModdingSession.ReforgeCost = ReforgeCost.empty(viewer)
        override val output: NekoStack? = null
    }

    private class Failure(viewer: Player, description: List<Component>) : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = description
        override val reforgeCost: ModdingSession.ReforgeCost = ReforgeCost.empty(viewer)
        override val output: NekoStack? = null
    }

    private class Success(viewer: Player, outputItem: NekoStack, description: List<Component>, cost: ModdingSession.ReforgeCost) : Base() {
        override val isSuccess: Boolean = true
        override val description: List<Component> = description
        override val reforgeCost: ModdingSession.ReforgeCost = cost
        override val output: NekoStack by NekoStackDelegates.copyOnRead(outputItem)
    }
}

internal object ReforgeCost {
    fun empty(viewer: Player): ModdingSession.ReforgeCost {
        return Empty(viewer)
    }

    /**
     * @param currencyAmount 要消耗的默认货币数量
     */
    fun simple(viewer: Player, currencyAmount: Double): ModdingSession.ReforgeCost {
        return Simple(viewer, currencyAmount)
    }

    private class Empty(viewer: Player) : ModdingSession.ReforgeCost {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = true
        override val description: List<Component> = listOf(MessageConstants.MSG_MODDING_COST_EMPTY.translate(viewer))
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain)
        )

        override fun toString(): String = toSimpleString()
    }

    private class Simple(viewer: Player, val currencyAmount: Double) : ModdingSession.ReforgeCost {
        override fun take(viewer: Player) {
            EconomyManager.take(viewer.uniqueId, currencyAmount)
        }

        override fun test(viewer: Player): Boolean {
            return EconomyManager.has(viewer.uniqueId, currencyAmount).getOrDefault(false)
        }

        override val description: List<Component> = listOf(
            MessageConstants.MSG_MODDING_COST_SIMPLE.arguments(
                TranslationArgument.numeric(currencyAmount.toInt() + 1) // +1 使边界情况看起来合理
            ).translate(viewer)
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
    fun changeable(session: SimpleModdingSession, cell: Cell, rule: CellRule): ModdingSession.Replace {
        return Changeable(session, cell, rule)
    }

    private val ZERO_MOCHA_FUNCTION: MochaFunction = MochaFunction { .0 }

    /**
     * 代表一个不可修改的 [ModdingSession.Replace].
     * 这里所指的不可修改, 指的是玩家无论使用什么耗材, 达成什么样的条件, 都无法修改对应的核孔.
     */
    private class Unchangeable(
        override val session: SimpleModdingSession,
        override val cell: Cell,
    ) : ModdingSession.Replace, KoinComponent {
        override val changeable: Boolean
            get() = false

        override val cellRule
            get() = CellRule.empty()

        // unchangeable 不需要任何花费 (?)
        override val total
            get() = ZERO_MOCHA_FUNCTION

        // unchangeable 虽然无法被修改, 但依然可以输入一个耗材.
        // 这允许其他的系统优雅的处理有关耗材的输入/取出的逻辑.
        override var originalInput: ItemStack? by OriginalInputDelegate(null)

        // unchangeable 无法被修改, 因此结果也永远不会改变
        override fun bake(): ModdingSession.Replace.Result = latestResult

        // unchangeable 无法被修改, 因此永远不存在 usableInput
        override val usableInput: NekoStack? = null

        // usableInput 已经永远为 null, 那么这个也一样
        override val augment: PortableCore? = null

        // 永远返回同样的结果: 核孔无法修改
        override var latestResult = ReforgeReplaceResult.failure(session.viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_UNCHANGEABLE)

        override fun getIngredientLevel(): Int = 0
        override fun getIngredientRarityNumber(): Double = .0

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("id", cell.getId()),
        )

        override fun toString(): String = toSimpleString()

        private inner class OriginalInputDelegate(
            private var backing: ItemStack?,
        ) : ReadWriteProperty<Unchangeable, ItemStack?> {
            override fun getValue(thisRef: Unchangeable, property: KProperty<*>): ItemStack? {
                return backing?.clone()
            }

            override fun setValue(thisRef: Unchangeable, property: KProperty<*>, value: ItemStack?) {
                backing = value?.clone()
            }
        }
    }

    private class Changeable(
        override val session: SimpleModdingSession,
        override val cell: Cell,
        override val cellRule: CellRule,
    ) : ModdingSession.Replace, KoinComponent {
        override val total: MochaFunction = cellRule.currencyCost.total.compile(session, this)

        override var originalInput: ItemStack? by OriginalInputDelegate(null)

        private inner class OriginalInputDelegate(
            private var backing: ItemStack?,
        ) : ReadWriteProperty<Changeable, ItemStack?> {
            override fun getValue(thisRef: Changeable, property: KProperty<*>): ItemStack? {
                return backing?.clone()
            }

            override fun setValue(thisRef: Changeable, property: KProperty<*>, value: ItemStack?) {
                backing = value?.clone()
                bake()
            }
        }

        override fun bake(): ModdingSession.Replace.Result {
            val replaceResult = executeReplace0(originalInput).also {
                latestResult = it
            }

            if (replaceResult.applicable) {
                usableInput = originalInput!!.shadowNeko(true)!!
            } else {
                usableInput = null
            }

            return replaceResult
        }

        override var usableInput: NekoStack? by NekoStackDelegates.nullableCopyOnWrite(null)

        override val augment: PortableCore?
            get() = usableInput?.components?.get(ItemComponentTypes.PORTABLE_CORE)

        override val changeable: Boolean
            get() = true

        override var latestResult: ModdingSession.Replace.Result by Delegates.observable(ReforgeReplaceResult.empty(viewer)) { _, old, new ->
            // session.logger.info("Replace (changeable) result updated: $old -> $new")
        }

        private val viewer: Player
            get() = session.viewer

        private fun executeReplace0(originalInput: ItemStack?): ModdingSession.Replace.Result {
            // 如果耗材为空, 则返回空结果
            if (originalInput.isEmpty()) {
                return ReforgeReplaceResult.empty(viewer)
            }

            // 如果源物品为空, 则返回内部错误
            val usableInput = session.usableInput ?: run {
                session.logger.error("Usable input is null, but an item is being replaced. This is a bug!")
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_ERR_INTERNAL_ERROR)
            }

            val itemRule = session.itemRule ?: run {
                session.logger.error("Item rule is null, but an item is being replaced. This is a bug!")
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_ERR_INTERNAL_ERROR)
            }

            // TODO 检查权限

            // 获取耗材中的便携核心
            val customNekoStack = originalInput.shadowNeko(true)
            val portableCore = customNekoStack?.portableCore ?: run {
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_NOT_PORTABLE_CORE)
            }

            // 获取源物品上的核孔
            val inputCells = usableInput.cells ?: run {
                session.logger.error("Usable input has no cells, but an item is being replaced. This is a bug!")
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_ERR_INTERNAL_ERROR)
            }

            // 源物品的核孔上 必须没有与便携核心相似的核心
            val inputCellsExcludingThis = inputCells.filter2 { it.getId() != cell.getId() }
            if (inputCellsExcludingThis.containSimilarCore(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_SIMILAR_CORE_PRESENT_ON_TARGET)
            }

            if (
                session.replaceParams
                    .filter { it.key != cell.getId() } // 排除掉当前的核孔
                    .any { it.value.usableInput?.portableCore?.wrapped?.similarTo(portableCore.wrapped) == true }
            ) {
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_SIMILAR_CORE_PRESENT_ON_INPUT)
            }

            // 便携式核心的类型 必须符合定制规则
            if (!cellRule.acceptableCores.test(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_CORE_INCOMPATIBLE_WITH_CELL)
            }

            // 便携式核心上面的所有元素 必须全部出现在被定制物品上
            if (cellRule.requireElementMatch) {
                val elementsOnInput = usableInput.elements
                // 这里要求耗材上只有一种元素, 并且元素是存在核心里面的
                val elementOnIngredient = (portableCore.wrapped as? AttributeCore)?.attribute?.element
                if (elementOnIngredient != null && elementOnIngredient !in elementsOnInput) {
                    return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_CORE_ELEMENT_INCOMPATIBLE_WITH_TARGET)
                }
            }

            // 被定制物品上储存的历史定制次数 必须小于等于定制规则
            val modCount = usableInput.reforgeHistory.modCount
            if (modCount >= itemRule.modLimit) {
                return ReforgeReplaceResult.failure(viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_TARGET_REACH_MOD_COUNT_LIMIT)
            }

            // 全部检查通过!
            return ReforgeReplaceResult.success(viewer, MessageConstants.MSG_MODDING_REPLACE_RESULT_SUCCESS)
        }

        override fun getIngredientLevel(): Int {
            return usableInput?.level ?: 0
        }

        override fun getIngredientRarityNumber(): Double {
            return usableInput?.rarity?.getKeyOrThrow()?.value?.let(session.table.rarityNumberMapping::get) ?: .0
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("id", cell.getId()),
        )

        override fun toString(): String = toSimpleString()
    }
}

private object ReforgeReplaceResult {
    /**
     * 空的结果. 当没有耗材输入时, 用这个.
     */
    fun empty(viewer: Player): ModdingSession.Replace.Result {
        return Empty(viewer)
    }

    fun failure(viewer: Player, description: List<ComponentLike>): ModdingSession.Replace.Result {
        return Simple(false, description.translate(viewer))
    }

    fun failure(viewer: Player, description: ComponentLike): ModdingSession.Replace.Result {
        return failure(viewer, listOf(description))
    }

    fun success(viewer: Player, description: List<ComponentLike>): ModdingSession.Replace.Result {
        return Simple(true, description.translate(viewer))
    }

    fun success(viewer: Player, description: ComponentLike): ModdingSession.Replace.Result {
        return success(viewer, listOf(description))
    }

    private abstract class Base : ModdingSession.Replace.Result {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("applicable", applicable),
            ExaminableProperty.of("description", description.plain),
        )

        override fun toString(): String = toSimpleString()
    }

    private class Empty(viewer: Player) : Base() {
        override val applicable: Boolean = false // 空气无法参与定制, 需要额外逻辑判断
        override val description: List<Component> = listOf(MessageConstants.MSG_MODDING_REPLACE_RESULT_EMPTY.translate(viewer))
    }

    private class Simple(
        override val applicable: Boolean,
        override val description: List<Component>,
    ) : Base()
}

private object ReforgeReplaceMap {
    /**
     * 返回一个空的 [ModdingSession.ReplaceMap].
     *
     * 当没有可以定制的核孔时, 使用这个.
     */
    fun empty(session: SimpleModdingSession): ModdingSession.ReplaceMap {
        return Empty(session)
    }

    /**
     * 返回一个一般的 [ModdingSession.ReplaceMap].
     *
     * 当存在可以定制的核孔时, 使用这个.
     */
    fun simple(session: SimpleModdingSession, data: LinkedHashMap<String, ModdingSession.Replace>): ModdingSession.ReplaceMap {
        return Simple(session, data)
    }

    private class Empty(
        private val session: SimpleModdingSession,
    ) : ModdingSession.ReplaceMap {
        // 使用 map 来存这些 replace 是为了让 replace 能够正常的接收玩家输入的物品.
        private val store: HashMap<String, ModdingSession.Replace> = HashMap()

        override val size: Int = 0
        override val keys: Set<String> = emptySet()
        override val values: Collection<ModdingSession.Replace> = emptyList()

        override fun get(id: String): ModdingSession.Replace {
            // 始终返回一个 unchangeable replace.
            return store.getOrPut(id) {
                val dummyCell = Cell.of(id) // it should never be accessed
                ReforgeReplace.unchangeable(session, dummyCell)
            }
        }

        override fun contains(id: String): Boolean = false
        override fun getAllInputs(): Array<ItemStack> = emptyArray()
        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Replace>> = emptyList<Map.Entry<String, ModdingSession.Replace>>().iterator()
        override fun toString(): String = toSimpleString()
    }

    private class Simple(
        private val session: SimpleModdingSession,
        private val data: LinkedHashMap<String, ModdingSession.Replace>,
    ) : ModdingSession.ReplaceMap {

        override val size: Int
            get() = data.size
        override val keys: Set<String>
            get() = data.keys
        override val values: Collection<ModdingSession.Replace>
            get() = data.values

        override fun get(id: String): ModdingSession.Replace {
            // the dummy cell should never be accessed
            return data.getOrPut(id) { ReforgeReplace.unchangeable(session, Cell.of(id)) }
        }

        override fun contains(id: String): Boolean {
            return data.containsKey(id)
        }

        override fun getAllInputs(): Array<ItemStack> {
            return data.values.mapNotNull { it.originalInput }.toTypedArray()
        }

        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Replace>> {
            return data.iterator()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("size", size),
            ExaminableProperty.of("keys", keys),
        )

        override fun toString(): String {
            return toSimpleString()
        }
    }
}
