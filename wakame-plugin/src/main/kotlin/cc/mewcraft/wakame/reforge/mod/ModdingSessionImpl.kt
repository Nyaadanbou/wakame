package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.attribute.composite.element
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.ItemCells
import cc.mewcraft.wakame.item.components.PortableCore
import cc.mewcraft.wakame.item.components.cells.*
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.reforge.mod.ModdingTable.*
import cc.mewcraft.wakame.util.*
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
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
    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.MOD)

    // 初始为 null
    override var originalInput: ItemStack? by OriginalInputDelegate(null)

    // 初始为 null
    override var usableInput: NekoStack? by UsableInputDelegate(null)

    // 初始为 ReplaceMap.empty() 表示还没有输入
    override var replaceParams: ModdingSession.ReplaceMap by Delegates.observable(ReforgeReplaceMap.empty(this)) { _, old, new ->
        logger.info("Session's replace parameters updated: $old -> $new")
    }

    // 从配置文件编译 MochaFunction
    override val totalFunction: MochaFunction = table.currencyCost.total.compile(this)

    // 初始为 ReforgeResult.empty() 表示还没有输入
    override var latestResult: ModdingSession.ReforgeResult by Delegates.observable(ReforgeResult.empty()) { _, old, new ->
        logger.info("Session's result updated: $old -> $new")
    }

    // 初始为 false
    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }

        logger.info("Session's frozen status updated: $new")
        return@vetoable true
    }

    private fun executeReforge0(): ModdingSession.ReforgeResult {
        return try {
            ReforgeOperation(this)
        } catch (e: Exception) {
            logger.error("An error occurred while executing reforge operation", e)
            ReforgeResult.failure("<red>内部错误.".mini)
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
        return usableInput?.components?.get(ItemComponentTypes.LEVEL)?.level ?: 0
    }

    override fun getSourceItemRarityNumber(): Double {
        return usableInput?.components?.get(ItemComponentTypes.RARITY)?.rarity?.let { table.rarityNumberMapping.get(it.key) } ?: .0
    }

    override fun getSourceItemTotalCellCount(): Int {
        return usableInput?.components?.get(ItemComponentTypes.CELLS)?.size ?: 0
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
        ExaminableProperty.of("sourceItem", usableInput),
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

            logger.info("Session's input item updated: ${old?.type} -> ${value?.type}")

            val usableInput0 = _value?.customNeko ?: run {
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
            replaceParams = createReplaceParameters(thisRef, inputItemCells, inputItemRule)
            executeReforge()
        }

        private fun createReplaceParameters(
            thisRef: SimpleModdingSession,
            inputCells: ItemCells,
            inputItemRule: ItemRule,
        ): ModdingSession.ReplaceMap {
            val data = mutableMapOf<String, ModdingSession.Replace>()
            for ((id, cell) in inputCells) {
                val rule = inputItemRule.cellRules[id]
                if (rule != null) {
                    data[id] = ReforgeReplace.changeable(thisRef, cell, rule)
                } else {
                    data[id] = ReforgeReplace.unchangeable(thisRef, cell)
                }
            }

            return ReforgeReplaceMap.simple(thisRef, data)
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

            logger.info("Session's usable input updated: ${old?.id} -> ${value?.id}")
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
        cost: ModdingSession.ReforgeCost,
    ): ModdingSession.ReforgeResult {
        return Success(outputItem, listOf("<gray>准备就绪!".mini), cost)
    }

    private abstract class Base : ModdingSession.ReforgeResult {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("successful", isSuccess),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("reforgeCost", reforgeCost),
            ExaminableProperty.of("output", output),
        )

        override fun toString(): String = toSimpleString()
    }

    private class Empty : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = listOf(
            "<gray>没有要定制的物品.".mini
        )
        override val reforgeCost: ModdingSession.ReforgeCost = ReforgeCost.empty()
        override val output: NekoStack? = null
    }

    private class Failure(
        description: List<Component>,
    ) : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = description
        override val reforgeCost: ModdingSession.ReforgeCost = ReforgeCost.empty()
        override val output: NekoStack? = null
    }

    private class Success(
        outputItem: NekoStack,
        description: List<Component>,
        cost: ModdingSession.ReforgeCost,
    ) : Base() {
        override val isSuccess: Boolean = true
        override val description: List<Component> = description
        override val reforgeCost: ModdingSession.ReforgeCost = cost
        override val output: NekoStack by NekoStackDelegates.copyOnRead(outputItem)
    }
}

internal object ReforgeCost {
    fun empty(): ModdingSession.ReforgeCost {
        return Empty()
    }

    /**
     * @param currencyAmount 要消耗的默认货币数量
     */
    fun simple(
        currencyAmount: Double,
    ): ModdingSession.ReforgeCost {
        return Simple(currencyAmount)
    }

    private class Empty : ModdingSession.ReforgeCost {
        override fun take(viewer: Player) {
            // do nothing
        }

        override fun test(viewer: Player): Boolean {
            return true
        }

        override val description: List<Component> = listOf(
            "<gray>没有资源花费.".mini
        )

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain)
        )

        override fun toString(): String = toSimpleString()
    }

    private class Simple(
        val currencyAmount: Double,
    ) : ModdingSession.ReforgeCost {
        override fun take(viewer: Player) {
            // TODO 实现 take, test, description
        }

        override fun test(viewer: Player): Boolean {
            return true
        }

        override val description: List<Component> = listOf(
            "<gray>花费: <yellow>${currencyAmount.toInt()} 金币".mini
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
    fun unchangeable(
        session: SimpleModdingSession,
        cell: Cell,
    ): ModdingSession.Replace {
        return Unchangeable(session, cell)
    }

    /**
     * 封装一个可以修改的核孔.
     */
    fun changeable(
        session: SimpleModdingSession,
        cell: Cell,
        rule: CellRule,
    ): ModdingSession.Replace {
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

        override val rule
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
        override var latestResult = ReforgeReplaceResult.failure("<gray>核孔无法修改.".mini)

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
        override val rule: CellRule,
    ) : ModdingSession.Replace, KoinComponent {
        override val total: MochaFunction = rule.currencyCost.total.compile(session, this)

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
                usableInput = originalInput!!.customNeko!!
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

        override var latestResult: ModdingSession.Replace.Result by Delegates.observable(ReforgeReplaceResult.empty()) { _, old, new ->
            session.logger.info("Replace (changeable) result updated: $old -> $new")
        }

        private fun executeReplace0(originalInput: ItemStack?): ModdingSession.Replace.Result {
            // 如果耗材为空, 则返回空结果
            if (originalInput.isEmpty()) {
                return ReforgeReplaceResult.empty()
            }

            // 如果源物品为空, 则返回内部错误
            val usableInput = session.usableInput ?: run {
                session.logger.error("Usable input is null, but an item is being replaced. This is a bug!")
                return ReforgeReplaceResult.failure("<red>内部错误.".mini)
            }

            // TODO 检查权限

            // 获取耗材中的便携核心
            val customNekoStack = originalInput?.customNeko
            val portableCore = customNekoStack?.components?.get(ItemComponentTypes.PORTABLE_CORE) ?: run {
                return ReforgeReplaceResult.failure("<gray>非便携核心.".mini)
            }

            // 获取源物品上的核孔
            val inputCells = usableInput.components.get(ItemComponentTypes.CELLS) ?: run {
                session.logger.error("Usable input has no cells, but an item is being replaced. This is a bug!")
                return ReforgeReplaceResult.failure("<red>内部错误.".mini)
            }

            // 源物品的核孔上 必须没有与便携核心相似的核心
            val inputCellsExcludingThis = inputCells.filter2 { it.getId() != cell.getId() }
            if (inputCellsExcludingThis.containSimilarCore(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure("<gray>物品存在相似核心.".mini)
            }

            if (session.replaceParams.containsCoreSimilarTo(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure("<gray>输入存在相似核心.".mini)
            }

            // 便携式核心的类型 必须符合定制规则
            if (!rule.acceptableCores.test(portableCore.wrapped)) {
                return ReforgeReplaceResult.failure("<gray>核孔不兼容此核心.".mini)
            }

            // 便携式核心上面的所有元素 必须全部出现在被定制物品上
            if (rule.requireElementMatch) {
                val elementsOnInput = usableInput.components.get(ItemComponentTypes.ELEMENTS)?.elements ?: emptySet()
                // 这里要求耗材上只有一种元素, 并且元素是存在核心里面的
                val elementOnIngredient = (portableCore.wrapped as? AttributeCore)?.attribute?.element
                if (elementOnIngredient != null && elementOnIngredient !in elementsOnInput) {
                    return ReforgeReplaceResult.failure("<gray>元素跟物品不相融.".mini)
                }
            }

            // 被定制物品上储存的历史定制次数 必须小于等于定制规则
            val modCount = usableInput.components.get(ItemComponentTypes.CELLS)?.get(cell.getId())?.getReforgeHistory()?.modCount ?: Int.MAX_VALUE
            if (modCount >= rule.modLimit) {
                return ReforgeReplaceResult.failure("<gray>核孔已消磨殆尽.".mini)
            }

            // 全部检查通过!
            return ReforgeReplaceResult.success("<green>准备就绪!".mini)
        }

        override fun getIngredientLevel(): Int {
            return usableInput?.components?.get(ItemComponentTypes.LEVEL)?.level ?: 0
        }

        override fun getIngredientRarityNumber(): Double {
            return usableInput?.components?.get(ItemComponentTypes.RARITY)?.rarity?.let { session.table.rarityNumberMapping.get(it.key) } ?: .0
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
    fun empty(): ModdingSession.Replace.Result {
        return Empty()
    }

    fun failure(description: List<Component>): ModdingSession.Replace.Result {
        return Simple(false, description)
    }

    fun failure(description: Component): ModdingSession.Replace.Result {
        return failure(listOf(description))
    }

    fun success(description: List<Component>): ModdingSession.Replace.Result {
        return Simple(true, description)
    }

    fun success(description: Component): ModdingSession.Replace.Result {
        return success(listOf(description))
    }

    private abstract class Base : ModdingSession.Replace.Result {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("applicable", applicable),
            ExaminableProperty.of("description", description.plain),
        )

        override fun toString(): String = toSimpleString()
    }

    private class Empty : Base() {
        override val applicable: Boolean = false // 空气无法参与定制, 需要额外逻辑判断
        override val description: List<Component> = listOf("<gray>没有耗材输入.".mini)
    }

    private class Simple(
        override val applicable: Boolean,
        override val description: List<Component>,
    ) : Base()
}

private object ReforgeReplaceMap {
    /**
     * 返回一个空的 [ReforgeReplaceMap].
     *
     * 当没有可以定制的核孔时, 使用这个.
     */
    fun empty(session: SimpleModdingSession): ModdingSession.ReplaceMap {
        return Empty(session)
    }

    /**
     * 返回一个一般的 [ReforgeReplaceMap].
     *
     * 当存在可以定制的核孔时, 使用这个.
     */
    fun simple(session: SimpleModdingSession, data: Map<String, ModdingSession.Replace> = HashMap()): ModdingSession.ReplaceMap {
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
        override fun containsCoreSimilarTo(core: Core): Boolean = false
        override fun getAllInputs(): Array<ItemStack> = emptyArray()
        override fun iterator(): Iterator<Map.Entry<String, ModdingSession.Replace>> = emptyList<Map.Entry<String, ModdingSession.Replace>>().iterator()
        override fun toString(): String = toSimpleString()
    }

    private class Simple(
        session: SimpleModdingSession,
        data: Map<String, ModdingSession.Replace>,
    ) : ModdingSession.ReplaceMap {
        val session = session
        val data = HashMap(data) // TODO 对 Gui 排序

        override val size: Int
            get() = data.size
        override val keys: Set<String>
            get() = data.keys
        override val values: Collection<ModdingSession.Replace>
            get() = data.values

        override fun get(id: String): ModdingSession.Replace {
            return data.getOrPut(id) {
                val dummyCell = Cell.of(id) // it should never be accessed
                val dummyRule = CellRule.empty()
                ReforgeReplace.changeable(session, dummyCell, dummyRule)
            }
        }

        override fun contains(id: String): Boolean {
            return data.containsKey(id)
        }

        override fun containsCoreSimilarTo(core: Core): Boolean {
            return data.values.any { replace ->
                replace.usableInput
                    ?.components
                    ?.get(ItemComponentTypes.PORTABLE_CORE)
                    ?.wrapped
                    ?.similarTo(core) == true
            }
        }

        override fun getAllInputs(): Array<ItemStack> {
            return data.values.mapNotNull { it.originalInput }.toTypedArray()
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
