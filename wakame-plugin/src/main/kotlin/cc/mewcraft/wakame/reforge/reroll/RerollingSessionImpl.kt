package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.economy.EconomyManager
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaTypes
import cc.mewcraft.wakame.item2.config.datagen.impl.MetaCoreContainer
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.Core
import cc.mewcraft.wakame.item2.getData
import cc.mewcraft.wakame.item2.getMeta
import cc.mewcraft.wakame.item2.koishTypeId
import cc.mewcraft.wakame.lang.translate
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.reforge.common.ReforgingStationConstants
import cc.mewcraft.wakame.util.ItemStackDelegates
import cc.mewcraft.wakame.util.adventure.plain
import cc.mewcraft.wakame.util.adventure.toSimpleString
import cc.mewcraft.wakame.util.decorate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslationArgument
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import team.unnamed.mocha.runtime.MochaFunction
import java.util.Collections.emptyList
import java.util.Collections.emptyMap
import java.util.Collections.emptySet
import java.util.stream.Stream
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class SimpleRerollingSession(
    override val table: RerollingTable,
    override val viewer: Player,
) : RerollingSession {
    val logger: Logger = LOGGER.decorate(prefix = ReforgingStationConstants.REROLLING_LOG_PREFIX)

    override val total: MochaFunction = table.currencyCost.compile(this)

    override var originalInput: ItemStack? by OriginalInputDelegate(null)

    override var usableInput: ItemStack? by UsableInputDelegate(null)

    override var itemRule: RerollingTable.ItemRule? = null

    override var selectionMap: RerollingSession.SelectionMap by Delegates.observable(SelectionMap.empty(this)) { _, old, new ->
        // logger.info("Selection map updated: $old -> $new")
    }

    override var latestResult: RerollingSession.ReforgeResult by Delegates.observable(ReforgeResult.empty(viewer)) { _, old, new ->
        // logger.info("Result status updated: $old -> $new")
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Trying to unfreeze a frozen session. This is a bug!")
            return@vetoable false
        }

        // logger.info("Frozen status updated: $old -> $new")
        return@vetoable true
    }

    private fun executeReforge0(): RerollingSession.ReforgeResult {
        return try {
            ReforgeOperation(this)
        } catch (e: Exception) {
            logger.error("An unknown error occurred while rerolling an item", e)
            ReforgeResult.error(viewer)
        }
    }

    override fun executeReforge(): RerollingSession.ReforgeResult {
        return executeReforge0().also { latestResult = it }
    }

    override fun reset() {
        originalInput = null
        selectionMap = SelectionMap.empty(this)
        latestResult = ReforgeResult.empty(viewer)
    }

    override fun getAllInputs(): Array<ItemStack> {
        val result = mutableListOf<ItemStack>()
        originalInput?.let(result::add)
        return result.toTypedArray()
    }

    override fun getFinalOutputs(): Array<ItemStack> {
        val reforgeResult = latestResult
        if (reforgeResult.isSuccess) {
            return arrayOf(reforgeResult.output.clone())
        } else {
            return emptyArray()
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("viewer", viewer.name),
        ExaminableProperty.of("table", table),
    )

    override fun toString(): String = toSimpleString()

    private inner class OriginalInputDelegate(private var backing: ItemStack?) : ReadWriteProperty<RerollingSession, ItemStack?> {
        override fun getValue(thisRef: RerollingSession, property: KProperty<*>): ItemStack? {
            return backing?.clone()
        }

        override fun setValue(thisRef: RerollingSession, property: KProperty<*>, value: ItemStack?) {
            backing = value?.clone()
            usableInput = value
            itemRule = usableInput?.koishTypeId?.let(table.itemRuleMap::get)
            selectionMap = SelectionMap.simple(thisRef)
            latestResult = executeReforge0()
        }
    }

    private inner class UsableInputDelegate(private var backing: ItemStack?) : ReadWriteProperty<RerollingSession, ItemStack?> {
        override fun getValue(thisRef: RerollingSession, property: KProperty<*>): ItemStack? {
            return backing?.clone()
        }

        override fun setValue(thisRef: RerollingSession, property: KProperty<*>, value: ItemStack?) {
            backing = value?.clone()
        }
    }
}

internal object ReforgeResult {
    /**
     * 空结果; 用于表示没有要重造的物品.
     */
    fun empty(viewer: Player): RerollingSession.ReforgeResult {
        return Empty(viewer)
    }

    /**
     * 错误结果; 用于表示重造过程中出现了内部错误.
     */
    fun error(viewer: Player): RerollingSession.ReforgeResult {
        return Error(viewer)
    }

    /**
     * 失败结果; 用于表示因已知的某些条件不满足而无法进行重造.
     */
    fun failure(viewer: Player, description: List<ComponentLike>): RerollingSession.ReforgeResult {
        return Simple(false, description.translate(viewer), ItemStack.empty(), ReforgeCost.empty(viewer))
    }

    /**
     * 参考 [ReforgeResult.failure].
     */
    fun failure(viewer: Player, description: ComponentLike): RerollingSession.ReforgeResult {
        return failure(viewer, listOf(description))
    }

    /**
     * 成功结果; 用于表示重造已准备就绪.
     */
    fun success(viewer: Player, item: ItemStack, cost: RerollingSession.ReforgeCost): RerollingSession.ReforgeResult {
        return Simple(true, TranslatableMessages.MSG_REROLLING_RESULT_SUCCESS.translate(viewer), item, cost)
    }

    private abstract class Base : RerollingSession.ReforgeResult {
        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("isSuccess", isSuccess),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("reforgeCost", reforgeCost),
            ExaminableProperty.of("output", output),
        )

        override fun toString(): String = toSimpleString()
    }

    // FIXME data class
    private class Error(viewer: Player) : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = listOf(TranslatableMessages.MSG_ERR_INTERNAL_ERROR.translate(viewer))
        override val reforgeCost: RerollingSession.ReforgeCost = ReforgeCost.error(viewer)
        override val output: ItemStack = ItemStack.empty()
    }

    private class Empty(viewer: Player) : Base() {
        override val isSuccess: Boolean = false
        override val description: List<Component> = listOf(TranslatableMessages.MSG_REROLLING_RESULT_EMPTY.translate(viewer))
        override val reforgeCost: RerollingSession.ReforgeCost = ReforgeCost.empty(viewer)
        override val output: ItemStack = ItemStack.empty()
    }

    private class Simple(
        successful: Boolean,
        description: List<Component>,
        item: ItemStack,
        cost: RerollingSession.ReforgeCost,
    ) : Base() {

        constructor(
            successful: Boolean,
            description: Component,
            item: ItemStack,
            cost: RerollingSession.ReforgeCost,
        ) : this(successful, listOf(description), item, cost)

        override val isSuccess: Boolean = successful
        override val description: List<Component> = description
        override val reforgeCost: RerollingSession.ReforgeCost = cost
        override val output: ItemStack by ItemStackDelegates.copyOnRead(item)

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("isSuccess", isSuccess),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("reforgeCost", reforgeCost),
            ExaminableProperty.of("output", output),
        )

        override fun toString(): String = toSimpleString()
    }
}

internal object ReforgeCost {
    /**
     * 空的花费; 当没有需要重造的物品时, 使用这个.
     */
    fun empty(viewer: Player): RerollingSession.ReforgeCost {
        return Empty(viewer)
    }

    /**
     * 错误的花费; 当重造过程中出现了内部错误, 使用这个.
     */
    fun error(viewer: Player): RerollingSession.ReforgeCost {
        return Error(viewer)
    }

    /**
     * 正常的花费; 当重造已经准备就绪时, 使用这个.
     */
    fun simple(viewer: Player, currencyAmount: Double): RerollingSession.ReforgeCost {
        return Simple(viewer, currencyAmount)
    }

    // FIXME data class
    private abstract class Base : RerollingSession.ReforgeCost {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain)
        )

        override fun toString(): String = toSimpleString()
    }

    private class Error(viewer: Player) : Base() {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = false
        override val description: List<Component> = listOf(TranslatableMessages.MSG_ERR_INTERNAL_ERROR.translate(viewer))
    }

    private class Empty(viewer: Player) : Base() {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = true
        override val description: List<Component> = listOf(TranslatableMessages.MSG_REROLLING_COST_EMPTY.translate(viewer))
    }

    private class Simple(viewer: Player, val amount: Double) : RerollingSession.ReforgeCost {
        override fun take(viewer: Player) {
            EconomyManager.take(viewer.uniqueId, amount)
        }

        override fun test(viewer: Player): Boolean {
            return EconomyManager.has(viewer.uniqueId, amount).getOrDefault(false)
        }

        override val description: List<Component> = listOf(TranslatableMessages.MSG_REROLLING_COST_SIMPLE.arguments(TranslationArgument.numeric(amount)).translate(viewer))

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("amount", amount)
        )

        override fun toString(): String = toSimpleString()
    }
}

internal object Selection {
    /**
     * 创建一个不可修改的 [Selection].
     */
    fun unchangeable(session: RerollingSession, id: String): RerollingSession.Selection {
        return Empty(session, id)
    }

    /**
     * 创建一个可被修改的 [Selection].
     */
    fun changeable(session: RerollingSession, id: String, rule: RerollingTable.CoreContainerRule, lootTable: LootTable<Core>): RerollingSession.Selection {
        return Simple(session, id, rule, lootTable)
    }

    // FIXME data class
    private class Empty(
        override val session: RerollingSession,
        override val id: String,
    ) : RerollingSession.Selection {
        override val rule: RerollingTable.CoreContainerRule
            get() = RerollingTable.CoreContainerRule.empty()
        override val changeable: Boolean
            get() = false
        override val lootTable: LootTable<Core>
            get() = LootTable.empty()
        override val total: MochaFunction
            get() = MochaFunction { .0 }
        override var selected: Boolean
            set(_) = Unit
            get() = false

        override fun invert(): Boolean = false
        override fun toString(): String = toSimpleString()
    }

    private class Simple(
        override val session: RerollingSession,
        override val id: String,
        override val rule: RerollingTable.CoreContainerRule,
        override val lootTable: LootTable<Core>,
    ) : RerollingSession.Selection {
        // private val logger: Logger = LOGGER.decorate(prefix = ReforgeLoggerPrefix.REROLL)
        override val total: MochaFunction = rule.currencyCost.compile(session, this)
        override val changeable: Boolean = true
        override var selected: Boolean by Delegates.observable(false) { _, old, new ->
            // logger.info("Selection status updated (core container: '$id'): $old -> $new")
        }

        override fun invert(): Boolean {
            val inverted = !selected
            selected = inverted
            return inverted
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("id", id),
            ExaminableProperty.of("selected", selected),
        )

        override fun toString(): String = toSimpleString()
    }
}

internal object SelectionMap {
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
        val usableInput = session.usableInput ?: return empty(session)

        // 获取源物品的核孔模板
        // 如果源物品没有核孔*模板*, 则判定整个物品不支持重造
        val metaCoreContainer = usableInput.getMeta(ItemMetaTypes.CORE_CONTAINER) ?: run {
            // logger.info("Usable input has no `core container` template.")
            return empty(session)
        }

        // 如果源物品的核孔模板不是动态的, 则判定整个物品不支持重造
        if (metaCoreContainer !is MetaCoreContainer.Dynamic) {
            return empty(session)
        }

        // 获取源物品的核孔
        // 如果这个物品没有核孔组件, 则判定整个物品不支持重造
        val coreContainer = usableInput.getData(ItemDataTypes.CORE_CONTAINER) ?: return empty(session)

        // 获取源物品的重造规则
        // 如果这个物品没有对应的重造规则, 则判定整个物品不支持重造
        val itemRule = session.table.itemRuleMap[usableInput.koishTypeId!!] ?: return empty(session)

        val coreContainerRuleMap = itemRule.coreContainerRuleMap
        val selectionData = sortedMapOf<String, RerollingSession.Selection>(coreContainerRuleMap.comparator)
        for ((id, _) in coreContainer) {

            // 获取核孔的重造规则
            val coreContainerRule = coreContainerRuleMap[id]

            // 获取核孔的重造模板
            val lootTable = metaCoreContainer.entry[id]

            // 如果:
            //   这个核孔没有对应的重造规则, 或者
            //   这个核孔没有对应的物品模板,
            // 则判定该核孔不支持重造.
            // 不支持重造的核孔依然被封装为一个 Selection.unchangeable,
            // 这样可以让其他系统比较优雅的处理一些特殊情况 (例如无定义情况).
            if (coreContainerRule != null && lootTable != null) {
                selectionData[id] = Selection.changeable(session, id, coreContainerRule, lootTable)
            } else {
                // logger.info("Item core container '$id' does not support rerolling.")
                selectionData[id] = Selection.unchangeable(session, id)
            }
        }

        return Simple(session, LinkedHashMap(selectionData))
    }

    // private val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.REROLL)

    // FIXME data class
    private class Empty(
        override val session: RerollingSession,
    ) : RerollingSession.SelectionMap {
        override val size: Int
            get() = 0
        override val keys: Set<String>
            get() = emptySet()
        override val values: Collection<RerollingSession.Selection>
            get() = emptyList()

        override fun get(id: String): RerollingSession.Selection =
            // 对于任何核孔, 返回 unchangeable
            Selection.unchangeable(session, id)

        override fun contains(id: String): Boolean =
            // 对于任何核孔, 都返回 false
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
        private val data: LinkedHashMap<String, RerollingSession.Selection>,
    ) : RerollingSession.SelectionMap {

        override val size: Int
            get() = data.size
        override val keys: Set<String>
            get() = data.keys
        override val values: Collection<RerollingSession.Selection>
            get() = data.values

        override fun get(id: String): RerollingSession.Selection {
            return data[id] ?: Selection.unchangeable(session, id)
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