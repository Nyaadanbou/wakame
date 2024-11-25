package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.composite.CompositeAttributeComponent
import cc.mewcraft.wakame.integration.economy.EconomyManager
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.util.decorate
import cc.mewcraft.wakame.util.plain
import cc.mewcraft.wakame.util.toSimpleString
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
import kotlin.properties.Delegates

internal class SimpleMergingSession(
    override val viewer: Player,
    override val table: MergingTable,
    inputItem1: NekoStack? = null,
    inputItem2: NekoStack? = null,
) : MergingSession, KoinComponent {
    private val logger: Logger = get<Logger>().decorate(prefix = ReforgeLoggerPrefix.MERGE)

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
            logger.error("Trying to return input item of a frozen merging session. This is a bug!")
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
                logger.error("Failed to return input item '$slot' to player '${viewer.name}'", e)
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
    override fun getFinalOutputs(): Array<ItemStack> {
        if (latestResult.isSuccess) {
            return arrayOf(latestResult.output.itemStack)
        } else {
            return emptyArray()
        }
    }

    override var latestResult: MergingSession.ReforgeResult by Delegates.vetoable(ReforgeResult.empty()) { _, old, new ->
        if (frozen) {
            logger.error("Trying to set result of a frozen merging session. This is a bug!")
            return@vetoable false
        }

        logger.info("Updating result: $new")
        return@vetoable true
    }

    private val _numberMergeFunctionMap = mutableMapOf<MergingTable.NumberMergeFunction.Type, MochaFunction>()
    override val numberMergeFunction: (MergingTable.NumberMergeFunction.Type) -> MochaFunction = { type ->
        _numberMergeFunctionMap.getOrPut(type) { table.numberMergeFunction.compile(type, this) }
    }
    override val outputLevelFunction: MochaFunction = table.outputLevelFunction.compile(this)
    override val outputPenaltyFunction: MochaFunction = table.outputPenaltyFunction.compile(this)
    override val currencyCostFunction: MochaFunction = table.currencyCost.total.compile(this)

    private fun executeReforge0(): MergingSession.ReforgeResult {
        return try {
            MergeOperation(this)
        } catch (e: Exception) {
            logger.error("An unknown error occurred while merging. This is a bug!", e)
            ReforgeResult.failure("<red>内部错误".mini)
        }
    }

    override fun executeReforge(): MergingSession.ReforgeResult {
        return executeReforge0().also { latestResult = it }
    }

    override fun reset() {
        inputItem1 = null
        inputItem2 = null
        latestResult = ReforgeResult.empty()
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Unfreezing a frozen merging session. This is a bug!")
            return@vetoable false
        }

        logger.info("Updating frozen state: $new")
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
 * 包含了构建各种 [MergingSession.ReforgeResult] 的方法.
 */
internal object ReforgeResult {

    /**
     * 构建一个用于表示*没有合并*的 [MergingSession.ReforgeResult].
     */
    fun empty(): MergingSession.ReforgeResult {
        return Simple(false, "<gray>没有要合并的核心.".mini, ReforgeType.empty(), ReforgeCost.zero(), NekoStack.empty())
    }

    /**
     * 构建一个用于表示*合并失败*的 [MergingSession.ReforgeResult].
     */
    fun failure(description: Component): MergingSession.ReforgeResult {
        return Simple(false, description, ReforgeType.failure(), ReforgeCost.failure(), NekoStack.empty())
    }

    /**
     * 构建一个用于表示*合并成功*的 [MergingSession.ReforgeResult].
     */
    fun success(item: NekoStack, type: MergingSession.ReforgeType, cost: MergingSession.ReforgeCost): MergingSession.ReforgeResult {
        return Simple(true, "<gray>准备就绪!".mini, type, cost, item)
    }

    private class Simple(
        isSuccess: Boolean,
        description: Component,
        type: MergingSession.ReforgeType,
        cost: MergingSession.ReforgeCost,
        output: NekoStack,
    ) : MergingSession.ReforgeResult {

        override val isSuccess = isSuccess
        override val description: Component = description
        override val reforgeType: MergingSession.ReforgeType = type
        override val reforgeCost = cost
        override val output: NekoStack by NekoStackDelegates.copyOnRead(output)

        override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
            ExaminableProperty.of("isSuccess", isSuccess),
            ExaminableProperty.of("description", description.plain),
            ExaminableProperty.of("reforgeType", reforgeType),
            ExaminableProperty.of("reforgeCost", reforgeCost),
            ExaminableProperty.of("output", output),
        )

        override fun toString(): String = toSimpleString()
    }
}

/**
 * 包含了构建各种 [MergingSession.ReforgeType] 的方法.
 */
internal object ReforgeType {

    /**
     * 构建一个用于表示没有合并的 [MergingSession.ReforgeType].
     */
    fun empty(): MergingSession.ReforgeType {
        return Empty()
    }

    /**
     * 构建一个用于表示合并失败的 [MergingSession.ReforgeType].
     */
    fun failure(): MergingSession.ReforgeType {
        return Failure()
    }

    /**
     * 通过 [AttributeModifier.Operation] 构建 [MergingSession.ReforgeType].
     */
    fun success(operation: AttributeModifier.Operation): MergingSession.ReforgeType {
        return when (operation) {
            AttributeModifier.Operation.ADD -> Success0()
            AttributeModifier.Operation.MULTIPLY_BASE -> Success1()
            AttributeModifier.Operation.MULTIPLY_TOTAL -> Success2()
        }
    }

    private abstract class Base : MergingSession.ReforgeType {
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
            "<gray>类型: <white>n/a".mini
        )

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("operation", "n/a"),
            ExaminableProperty.of("description", description.plain),
        )
    }

    private class Failure : Base() {
        override val operation: AttributeModifier.Operation
            get() = throw IllegalStateException("This type is not supposed to be used.")
        override val description: List<Component> = listOf(
            "<gray>类型: <white>n/a".mini
        )

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("operation", "n/a"),
            ExaminableProperty.of("description", description.plain),
        )
    }

    private class Success0 : Base() {
        override val operation: AttributeModifier.Operation =
            AttributeModifier.Operation.ADD
        override val description: List<Component> = listOf(
            "<gray>类型: <white>type 0".mini
        )
    }

    private class Success1 : Base() {
        override val operation: AttributeModifier.Operation =
            AttributeModifier.Operation.MULTIPLY_BASE
        override val description: List<Component> = listOf(
            "<gray>类型: <white>type 1".mini
        )
    }

    private class Success2 : Base() {
        override val operation: AttributeModifier.Operation =
            AttributeModifier.Operation.MULTIPLY_TOTAL
        override val description: List<Component> = listOf(
            "<gray>类型: <white>type 2".mini
        )
    }
}

/**
 * 包含了构建各种 [MergingSession.ReforgeCost] 的方法.
 */
internal object ReforgeCost {
    /**
     * 表示没有资源消耗.
     */
    fun zero(): MergingSession.ReforgeCost {
        return Zero
    }

    /**
     * 表示由于合并失败而产生的资源消耗.
     */
    fun failure(): MergingSession.ReforgeCost {
        return Failure
    }

    /**
     * 表示由于合并成功而产生的资源消耗.
     */
    fun success(defaultCurrencyAmount: Double): MergingSession.ReforgeCost {
        return Success(defaultCurrencyAmount)
    }

    private abstract class Base : MergingSession.ReforgeCost {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain),
        )

        override fun toString(): String = toSimpleString()
    }

    private object Zero : Base() {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = true
        override val description: List<Component> = listOf(
            "<gray>花费: <white>n/a".mini
        )
    }

    private object Failure : Base() {
        override fun take(viewer: Player): Unit =
            throw IllegalStateException("This cost is not supposed to be taken.")

        override fun test(viewer: Player): Boolean =
            throw IllegalStateException("This cost is not supposed to be tested.")

        override val description: List<Component> = listOf(
            "<gray>花费: <white>n/a".mini
        )
    }

    // 2024/8/12 TBD
    // 支持多货币
    // 支持自定义物品
    private class Success(
        val currencyAmount: Double,
    ) : Base() {
        override fun take(viewer: Player) {
            EconomyManager.take(viewer.uniqueId, currencyAmount)
        }

        override fun test(viewer: Player): Boolean {
            return EconomyManager.has(viewer.uniqueId, currencyAmount).getOrDefault(false)
        }

        override val description: List<Component> = listOf(
            "<gray>花费: <yellow>${currencyAmount.toInt()} 金币".mini
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