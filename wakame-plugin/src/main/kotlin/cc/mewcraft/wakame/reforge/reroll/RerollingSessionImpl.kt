package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.templates.components.cells.CoreBlueprint
import cc.mewcraft.wakame.random3.Group
import cc.mewcraft.wakame.reforge.common.CoreIcons
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
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

internal class SimpleRerollingSession(
    override val table: RerollingTable,
    override val viewer: Player,
    sourceItem: NekoStack? = null,
) : RerollingSession, KoinComponent {
    val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.REROLL)

    override val total: MochaFunction = table.currencyCost.compile(this)

    override var inputItem: ItemStack?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var sourceItem: NekoStack? by SourceItemDelegate(sourceItem)

    override var selectionMap: RerollingSession.SelectionMap by Delegates.observable(SelectionMap.empty(this)) { _, old, new ->
        logger.info("Selection map updated: $old -> $new")
    }

    override var latestResult: RerollingSession.ReforgeResult by Delegates.observable(ReforgeResult.empty()) { _, old, new ->
        logger.info("Result status updated: $old -> $new")
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }

        logger.info("Frozen status updated: $old -> $new")
        return@vetoable true
    }

    private fun executeReforge0(): RerollingSession.ReforgeResult {
        return try {
            ReforgeOperation(this)
        } catch (e: Exception) {
            logger.error("An unknown error occurred while rerolling an item", e)
            ReforgeResult.error()
        }
    }

    override fun executeReforge(): RerollingSession.ReforgeResult {
        return executeReforge0().also { latestResult = it }
    }

    override fun reset() {
        sourceItem = null
        selectionMap = SelectionMap.empty(this)
        latestResult = ReforgeResult.empty()
    }

    override fun getAllInputs(): Array<ItemStack> {
        val result = mutableListOf<ItemStack>()
        sourceItem?.itemStack?.let(result::add)
        return result.toTypedArray()
    }

    override fun getUnusedInputs(): Array<ItemStack> {
        return emptyArray() // 未来可能会用到?
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("viewer", viewer.name),
        ExaminableProperty.of("table", table),
    )

    override fun toString(): String = toSimpleString()

    private inner class SourceItemDelegate(
        private var backing: NekoStack?,
    ) : ReadWriteProperty<RerollingSession, NekoStack?> {
        override fun getValue(thisRef: RerollingSession, property: KProperty<*>): NekoStack? {
            return backing?.clone()
        }

        override fun setValue(thisRef: RerollingSession, property: KProperty<*>, value: NekoStack?) {
            backing = value?.clone()
            selectionMap = SelectionMap.simple(thisRef)
            latestResult = executeReforge0()
        }
    }
}

internal object ReforgeResult {
    /**
     * 空结果; 用于表示没有要重造的物品.
     */
    fun empty(): RerollingSession.ReforgeResult {
        return Empty()
    }

    /**
     * 错误结果; 用于表示重造过程中出现了内部错误.
     */
    fun error(): RerollingSession.ReforgeResult {
        return Error()
    }

    /**
     * 失败结果; 用于表示因已知的某些条件不满足而无法进行重造.
     */
    fun failure(
        description: List<Component>,
    ): RerollingSession.ReforgeResult {
        return Simple(false, description, NekoStack.empty(), ReforgeCost.empty())
    }

    /**
     * 参考 [ReforgeResult.failure].
     */
    fun failure(
        description: Component,
    ): RerollingSession.ReforgeResult {
        return failure(listOf(description))
    }

    /**
     * 成功结果; 用于表示重造已准备就绪.
     */
    fun success(
        item: NekoStack,
        cost: RerollingSession.ReforgeCost,
    ): RerollingSession.ReforgeResult {
        return Simple(true, "<gray>准备就绪!".mini, item, cost)
    }

    private abstract class Base : RerollingSession.ReforgeResult {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("successful", isSuccess),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("item", outputItem),
            ExaminableProperty.of("cost", reforgeCost),
        )

        override fun toString(): String = toSimpleString()
    }

    private class Empty : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = listOf(Component.text("<gray>没有输入."))
        override val outputItem: NekoStack = NekoStack.empty()
        override val reforgeCost: RerollingSession.ReforgeCost = ReforgeCost.empty()
    }

    private class Error : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = listOf(Component.text("<red>内部错误."))
        override val outputItem: NekoStack = NekoStack.empty()
        override val reforgeCost: RerollingSession.ReforgeCost = ReforgeCost.error()
    }

    private class Simple(
        successful: Boolean,
        description: List<Component>,
        item: NekoStack,
        cost: RerollingSession.ReforgeCost,
    ) : Base() {

        constructor(
            successful: Boolean,
            description: Component,
            item: NekoStack,
            cost: RerollingSession.ReforgeCost,
        ) : this(successful, listOf(description), item, cost)

        override val isSuccess: Boolean = successful
        override val description: List<Component> = description
        override val outputItem: NekoStack by NekoStackDelegates.copyOnRead(item)
        override val reforgeCost: RerollingSession.ReforgeCost = cost

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("successful", isSuccess),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("cost", reforgeCost),
            ExaminableProperty.of("item", outputItem),
        )

        override fun toString(): String = toSimpleString()
    }
}

internal object ReforgeCost {
    /**
     * 空的花费; 当没有需要重造的物品时, 使用这个.
     */
    fun empty(): RerollingSession.ReforgeCost {
        return Empty()
    }

    /**
     * 错误的花费; 当重造过程中出现了内部错误, 使用这个.
     */
    fun error(): RerollingSession.ReforgeCost {
        return Error()
    }

    /**
     * 正常的花费; 当重造已经准备就绪时, 使用这个.
     */
    fun simple(
        currencyAmount: Double,
    ): RerollingSession.ReforgeCost {
        return Simple(currencyAmount)
    }

    private abstract class Base : RerollingSession.ReforgeCost {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain)
        )

        override fun toString(): String = toSimpleString()
    }

    private class Empty : Base() {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = true
        override val description: List<Component> = listOf("<gray>花费: <white>无".mini)
    }

    private class Error : Base() {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = false
        override val description: List<Component> = listOf("<gray>花费: <red>内部错误".mini)
    }

    private class Simple(
        val currencyAmount: Double,
    ) : RerollingSession.ReforgeCost {
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

internal object Selection {
    /**
     * 创建一个不可修改的 [Selection].
     */
    fun unchangeable(session: RerollingSession): RerollingSession.Selection {
        return Empty(session)
    }

    /**
     * 创建一个可被修改的 [Selection].
     */
    fun changeable(
        session: RerollingSession,
        id: String,
        rule: RerollingTable.CellRule,
        template: Group<CoreBlueprint, ItemGenerationContext>,
        display: ItemStack,
    ): RerollingSession.Selection {
        return Simple(session, id, rule, template, display)
    }

    private class Empty(
        override val session: RerollingSession,
    ) : RerollingSession.Selection {
        override val id: String
            get() = "" // ???
        override val rule: RerollingTable.CellRule
            get() = RerollingTable.CellRule.empty()
        override val template: Group<CoreBlueprint, ItemGenerationContext>
            get() = Group.empty()
        override val display: ItemStack
            get() = ItemStack.empty()
        override val total: MochaFunction
            get() = MochaFunction { .0 }
        override val changeable: Boolean
            get() = false
        override var selected: Boolean
            set(_) = Unit
            get() = false

        override fun invert(): Boolean = false
        override fun toString(): String = toSimpleString()
    }

    private class Simple(
        override val session: RerollingSession,
        override val id: String,
        override val rule: RerollingTable.CellRule,
        override val template: Group<CoreBlueprint, ItemGenerationContext>,
        override val display: ItemStack,
    ) : RerollingSession.Selection, KoinComponent {
        private val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.REROLL)
        override val total: MochaFunction = rule.currencyCost.compile(session, this)
        override val changeable: Boolean
            get() = true
        override var selected: Boolean by Delegates.observable(false) { _, old, new ->
            logger.info("Selection status updated (cell: '$id'): $old -> $new")
        }

        override fun invert(): Boolean {
            val inverted = !selected
            selected = inverted
            return inverted
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("rule", rule),
            ExaminableProperty.of("group", template),
            ExaminableProperty.of("selected", selected),
        )

        override fun toString(): String = toSimpleString()
    }
}

internal object SelectionMap : KoinComponent {
    /**
     * 创建一个空的 [SelectionMap].
     */
    fun empty(session: RerollingSession): RerollingSession.SelectionMap {
        return Empty(session)
    }

    /**
     * 创建一个普通的 [SelectionMap].
     *
     * 该函数会根据 [session] 的具体状态, 选择性返回空的 [SelectionMap].
     */
    fun simple(session: RerollingSession): RerollingSession.SelectionMap {
        // 获取源物品
        // 如果源物品不存在, 则直接返回空容器
        val sourceItem = session.sourceItem ?: return empty(session)

        // 获取源物品的词条栏模板
        // 如果源物品没有词条栏*模板*, 则判定整个物品不支持重造
        val templates = sourceItem.templates.get(ItemTemplateTypes.CELLS)?.cells ?: run {
            logger.info("Source item has no `cells` template.")
            return empty(session)
        }

        // 获取源物品的词条栏
        // 如果这个物品没有词条栏组件, 则判定整个物品不支持重造
        val cells = sourceItem.components.get(ItemComponentTypes.CELLS) ?: return empty(session)

        // 获取源物品的重造规则
        // 如果这个物品没有对应的重造规则, 则判定整个物品不支持重造
        val itemRule = session.table.itemRuleMap[sourceItem.id] ?: return empty(session)

        val selectionMap = Simple(session)
        for ((id, cell) in cells) {

            // 获取词条栏的重造规则
            // 如果这个词条栏没有对应的重造规则, 则判定该词条栏不支持重造
            val cellRule = itemRule.cellRuleMap[id] ?: continue

            // 获取词条栏的重造模板
            // 这个词条栏没有对应的模板, 则判定该词条栏不支持重造
            val template = templates[id]?.core ?: continue

            val display = ItemStack(CoreIcons.get(cell.hashCode()))
            display.editMeta {
                // TODO reforge2
                val name = cell.getId().mini
                val lore = listOf(cell.getCore().id.asString().mini)
                it.itemName(name)
                it.lore(lore)
                it.hideAllFlags()
            }

            val selection = Selection.changeable(
                session = session,
                id = id,
                rule = cellRule,
                template = template,
                display = display,
            )

            selectionMap[id] = selection
        }

        return selectionMap
    }

    private val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.REROLL)

    private class Empty(
        override val session: RerollingSession,
    ) : RerollingSession.SelectionMap {
        override val size: Int
            get() = 0
        override val keys: Set<String>
            get() = emptySet()
        override val values: Collection<RerollingSession.Selection>
            get() = emptyList()
        override val isEmpty: Boolean
            get() = true

        override fun get(id: String): RerollingSession.Selection? =
            null

        override fun contains(id: String): Boolean =
            false

        override fun iterator(): Iterator<Map.Entry<String, RerollingSession.Selection>> =
            emptyMap<String, RerollingSession.Selection>().iterator()

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("session", session),
        )

        override fun toString(): String = toSimpleString()
    }

    private class Simple(
        override val session: RerollingSession,
    ) : RerollingSession.SelectionMap, KoinComponent {
        private val data: HashMap<String, RerollingSession.Selection> = HashMap()

        override val size: Int
            get() = data.size
        override val keys: Set<String>
            get() = data.keys
        override val values: Collection<RerollingSession.Selection>
            get() = data.values
        override val isEmpty: Boolean
            get() = data.isEmpty()

        override fun get(id: String): RerollingSession.Selection? {
            return data[id]
        }

        // exposed for implementation
        operator fun set(id: String, selection: RerollingSession.Selection) {
            data[id] = selection
        }

        override fun contains(id: String): Boolean {
            return data.containsKey(id)
        }

        override fun iterator(): Iterator<Map.Entry<String, RerollingSession.Selection>> {
            return data.iterator()
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("session", session),
            ExaminableProperty.of("data", data),
        )

        override fun toString(): String = toSimpleString()
    }
}