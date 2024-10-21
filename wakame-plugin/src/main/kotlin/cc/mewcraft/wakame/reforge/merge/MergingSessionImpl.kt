package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.composite.CompositeAttributeComponent
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.util.plain
import cc.mewcraft.wakame.util.toSimpleString
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.Component
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import team.unnamed.mocha.runtime.MochaFunction
import java.util.stream.Stream
import kotlin.properties.Delegates

internal class SimpleMergingSession(
    override val viewer: Player,
    override val table: MergingTable,
    inputItem1: NekoStack? = null,
    inputItem2: NekoStack? = null,
) : MergingSession, KoinComponent {
    private val logger: Logger by inject()

    companion object {
        private const val PREFIX = ReforgeLoggerPrefix.MERGE
    }

    override var inputItem1: NekoStack? by NekoStackDelegates.nullableCopyOnWrite(inputItem1)
    override var inputItem2: NekoStack? by NekoStackDelegates.nullableCopyOnWrite(inputItem2)

    private enum class InputSlot {
        INPUT1, INPUT2
    }

    private fun returnInputItem(
        viewer: Player,
        slot: InputSlot,
    ) {
        if (frozen) {
            logger.error("$PREFIX Trying to return input item of a frozen merging session. This is a bug!")
            return
        }

        val item = when (slot) {
            InputSlot.INPUT1 -> inputItem1
            InputSlot.INPUT2 -> inputItem2
        }?.itemStack

        if (item != null) {
            try {
                viewer.inventory.addItem(item)
            } catch (e: Exception) {
                logger.error("$PREFIX Failed to return input item '$slot' to player '${viewer.name}'", e)
            } finally {
                when (slot) {
                    InputSlot.INPUT1 -> inputItem1 = null
                    InputSlot.INPUT2 -> inputItem2 = null
                }
            }
        }
    }

    override fun returnInputItem1(viewer: Player) = returnInputItem(viewer, InputSlot.INPUT1)
    override fun returnInputItem2(viewer: Player) = returnInputItem(viewer, InputSlot.INPUT2)

    override var latestResult: MergingSession.Result by Delegates.vetoable(ReforgeResult.empty()) { _, old, new ->
        if (frozen) {
            logger.error("$PREFIX Trying to set result of a frozen merging session. This is a bug!")
            return@vetoable false
        }

        logger.info("$PREFIX Updating result: $new")
        return@vetoable true
    }

    private val _numberMergeFunctionMap = mutableMapOf<MergingTable.NumberMergeFunction.Type, MochaFunction>()
    override val numberMergeFunction: (MergingTable.NumberMergeFunction.Type) -> MochaFunction = { type ->
        _numberMergeFunctionMap.getOrPut(type) { table.numberMergeFunction.compile(type, this) }
    }
    override val outputLevelFunction: MochaFunction = table.outputLevelFunction.compile(this)
    override val outputPenaltyFunction: MochaFunction = table.outputPenaltyFunction.compile(this)
    override val currencyCostFunction: MochaFunction = table.currencyCost.total.compile(this)

    private fun executeReforge0(): MergingSession.Result {
        return try {
            MergeOperation(this)
        } catch (e: Exception) {
            logger.error("$PREFIX An unknown error occurred while merging. This is a bug!", e)
            ReforgeResult.failure("<red>内部错误".mini)
        }
    }

    override fun executeReforge(): MergingSession.Result {
        return executeReforge0().also { latestResult = it }
    }

    override fun reset() {
        inputItem1 = null
        inputItem2 = null
        latestResult = ReforgeResult.empty()
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("$PREFIX Unfreezing a frozen merging session. This is a bug!")
            return@vetoable false
        }

        logger.info("$PREFIX Updating frozen state: $new")
        return@vetoable true
    }

    @Suppress("UNCHECKED_CAST")
    private fun getValue(inputItem: NekoStack?): Double {
        val comp = inputItem?.components?.get(ItemComponentTypes.PORTABLE_CORE) ?: return .0
        val core = comp.wrapped as? AttributeCore ?: return .0
        val scalar = core.attribute as? CompositeAttributeComponent.Scalar<Double> ?: return .0
        val value = scalar.value
        return value
    }

    private fun getLevel(inputItem: NekoStack?): Double {
        val comp = inputItem?.components?.get(ItemComponentTypes.LEVEL) ?: return .0
        val value = comp.level.toDouble()
        return value
    }

    private fun getRarityNumber(inputItem: NekoStack?): Double {
        val comp = inputItem?.components?.get(ItemComponentTypes.RARITY) ?: return .0
        val rarityKey = comp.rarity.key
        val value = table.rarityNumberMapping.get(rarityKey)
        return value
    }

    private fun getPenalty(inputItem: NekoStack?): Double {
        val comp = inputItem?.components?.get(ItemComponentTypes.PORTABLE_CORE) ?: return .0
        val value = comp.penalty.toDouble()
        return value
    }

    override fun getValue1(): Double = getValue(inputItem1)
    override fun getValue2(): Double = getValue(inputItem2)
    override fun getLevel1(): Double = getLevel(inputItem1)
    override fun getLevel2(): Double = getLevel(inputItem2)
    override fun getRarityNumber1(): Double = getRarityNumber(inputItem1)
    override fun getRarityNumber2(): Double = getRarityNumber(inputItem2)
    override fun getPenalty1(): Double = getPenalty(inputItem1)
    override fun getPenalty2(): Double = getPenalty(inputItem2)

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("viewer", viewer),
        ExaminableProperty.of("table", table),
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 包含了构建各种 [MergingSession.Result] 的方法.
 */
internal object ReforgeResult {

    /**
     * 构建一个用于表示*没有合并*的 [MergingSession.Result].
     */
    fun empty(): MergingSession.Result {
        return Result(false, "<gray>没有可以合并的东西.".mini, NekoStack.empty(), ReforgeType.empty(), ReforgeCost.zero())
    }

    /**
     * 构建一个用于表示*合并失败*的 [MergingSession.Result].
     */
    fun failure(description: Component): MergingSession.Result {
        return Result(false, description, NekoStack.empty(), ReforgeType.failure(), ReforgeCost.failure())
    }

    /**
     * 构建一个用于表示*合并成功*的 [MergingSession.Result].
     */
    fun success(item: NekoStack, type: MergingSession.Type, cost: MergingSession.Cost): MergingSession.Result {
        return Result(true, "<gray>合并已准备就绪!".mini, item, type, cost)
    }

    private class Result(
        successful: Boolean,
        description: Component,
        item: NekoStack,
        type: MergingSession.Type,
        cost: MergingSession.Cost,
    ) : MergingSession.Result {

        override val successful = successful
        override val description: Component = description
        override val item: NekoStack by NekoStackDelegates.copyOnRead(item)
        override val type: MergingSession.Type = type
        override val cost = cost

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("successful", successful),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("item", item),
            ExaminableProperty.of("type", type),
            ExaminableProperty.of("cost", cost),
        )

        override fun toString(): String = toSimpleString()
    }
}

/**
 * 包含了构建各种 [MergingSession.Type] 的方法.
 */
internal object ReforgeType {

    /**
     * 构建一个用于表示没有合并的 [MergingSession.Type].
     */
    fun empty(): MergingSession.Type {
        return Empty()
    }

    /**
     * 构建一个用于表示合并失败的 [MergingSession.Type].
     */
    fun failure(): MergingSession.Type {
        return Failure()
    }

    /**
     * 通过 [AttributeModifier.Operation] 构建 [MergingSession.Type].
     */
    fun success(operation: AttributeModifier.Operation): MergingSession.Type {
        return when (operation) {
            AttributeModifier.Operation.ADD -> Success0()
            AttributeModifier.Operation.MULTIPLY_BASE -> Success1()
            AttributeModifier.Operation.MULTIPLY_TOTAL -> Success2()
        }
    }

    private abstract class Base : MergingSession.Type {
        override fun examinableProperties(): Stream<out ExaminableProperty?> {
            return Stream.of(
                ExaminableProperty.of("operation", operation),
                ExaminableProperty.of("description", description.plain),
            )
        }

        override fun toString(): String = toSimpleString()
    }

    private class Empty : Base() {
        override val operation: AttributeModifier.Operation
            get() = throw IllegalStateException("This type is not supposed to be used.")
        override val description: List<Component> = listOf(
            "<white>类型: <gray>N/A".mini
        )

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("operation", "N/A"),
            ExaminableProperty.of("description", description.plain),
        )
    }

    private class Failure : Base() {
        override val operation: AttributeModifier.Operation
            get() = throw IllegalStateException("This type is not supposed to be used.")
        override val description: List<Component> = listOf(
            "<white>类型: <gray>N/A".mini
        )

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("operation", "N/A"),
            ExaminableProperty.of("description", description.plain),
        )
    }

    private class Success0 : Base() {
        override val operation: AttributeModifier.Operation =
            AttributeModifier.Operation.ADD
        override val description: List<Component> = listOf(
            "<white>类型: <gray>Type 0".mini
        )
    }

    private class Success1 : Base() {
        override val operation: AttributeModifier.Operation =
            AttributeModifier.Operation.MULTIPLY_BASE
        override val description: List<Component> = listOf(
            "<white>类型: <gray>Type 1".mini
        )
    }

    private class Success2 : Base() {
        override val operation: AttributeModifier.Operation =
            AttributeModifier.Operation.MULTIPLY_TOTAL
        override val description: List<Component> = listOf(
            "<white>类型: <gray>Type 2".mini
        )
    }
}

/**
 * 包含了构建各种 [MergingSession.Cost] 的方法.
 */
internal object ReforgeCost {
    /**
     * 表示没有资源消耗.
     */
    fun zero(): MergingSession.Cost {
        return Zero
    }

    /**
     * 表示由于合并失败而产生的资源消耗.
     */
    fun failure(): MergingSession.Cost {
        return Failure
    }

    /**
     * 表示由于合并成功而产生的资源消耗.
     */
    fun success(defaultCurrencyAmount: Double): MergingSession.Cost {
        return Success(defaultCurrencyAmount)
    }

    private abstract class Base : MergingSession.Cost {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain),
        )

        override fun toString(): String = toSimpleString()
    }

    private object Zero : Base() {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = true
        override val description: List<Component> = listOf(
            "<white>花费: <gray>N/A".mini
        )
    }

    private object Failure : Base() {
        override fun take(viewer: Player): Unit =
            throw IllegalStateException("This cost is not supposed to be taken.")

        override fun test(viewer: Player): Boolean =
            throw IllegalStateException("This cost is not supposed to be tested.")

        override val description: List<Component> = listOf(
            "<white>花费: <gray>N/A".mini
        )
    }

    // 2024/8/12 TBD
    // 支持多货币
    // 支持自定义物品
    private class Success(
        val currencyAmount: Double,
    ) : Base() {
        override fun take(viewer: Player) {
            // TODO 实现 take, 还有下面的 test
        }

        override fun test(viewer: Player): Boolean {
            return true
        }

        override val description: List<Component> = listOf(
            "<white>花费: <yellow>${currencyAmount.toInt()} 金币".mini
        )

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.concat(
            super.examinableProperties(),
            Stream.of(
                ExaminableProperty.of("currencyAmount", currencyAmount),
            )
        )

        override fun toString(): String = toSimpleString()
    }
}