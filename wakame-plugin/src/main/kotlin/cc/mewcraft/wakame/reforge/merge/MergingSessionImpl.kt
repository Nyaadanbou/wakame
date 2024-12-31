package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.adventure.translator.MessageConstants
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.attribute.composite.CompositeAttributeComponent
import cc.mewcraft.wakame.integration.economy.EconomyManager
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.components.cells.AttributeCore
import cc.mewcraft.wakame.item.level
import cc.mewcraft.wakame.item.portableCore
import cc.mewcraft.wakame.item.rarity
import cc.mewcraft.wakame.item.reforgeHistory
import cc.mewcraft.wakame.lang.translate
import cc.mewcraft.wakame.reforge.common.ReforgeLoggerPrefix
import cc.mewcraft.wakame.util.decorate
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

    override var latestResult: MergingSession.ReforgeResult by Delegates.vetoable(ReforgeResult.empty(viewer)) { _, old, new ->
        if (frozen) {
            logger.error("Trying to set result of a frozen merging session. This is a bug!")
            return@vetoable false
        }

        // logger.info("Updating result: $new")
        return@vetoable true
    }

    private val _valueMergeFunction = HashMap<AttributeModifier.Operation, MergingTable.ValueMergeMethod.Algorithm>()
    override val valueMergeFunction: (AttributeModifier.Operation) -> MergingTable.ValueMergeMethod.Algorithm = { type ->
        _valueMergeFunction.getOrPut(type) { table.valueMergeMethod.compile(type, this) }
    }

    override val outputLevelFunction: MochaFunction = table.levelMergeMethod.compile(this)
    override val outputPenaltyFunction: MochaFunction = table.penaltyMergeMethod.compile(this)
    override val currencyCostFunction: MochaFunction = table.totalCost.compile(this)

    private fun executeReforge0(): MergingSession.ReforgeResult {
        return try {
            MergeOperation(this)
        } catch (e: Exception) {
            logger.error("An unknown error occurred while merging. This is a bug!", e)
            ReforgeResult.failure(viewer, MessageConstants.MSG_ERR_INTERNAL_ERROR)
        }
    }

    override fun executeReforge(): MergingSession.ReforgeResult {
        return executeReforge0().also { latestResult = it }
    }

    override fun reset() {
        inputItem1 = null
        inputItem2 = null
        latestResult = ReforgeResult.empty(viewer)
    }

    override var frozen: Boolean by Delegates.vetoable(false) { _, old, new ->
        if (!new && old) {
            logger.error("Unfreezing a frozen merging session. This is a bug!")
            return@vetoable false
        }

        // logger.info("Updating frozen state: $new")
        return@vetoable true
    }

    @Suppress("UNCHECKED_CAST")
    private fun getValue(inputItem: NekoStack?): Double {
        val core = inputItem?.portableCore?.wrapped as? AttributeCore ?: return .0
        val scalar = core.attribute as? CompositeAttributeComponent.Scalar<Double>
        return scalar?.value ?: .0
    }

    // 物品不存在, 返回 .0
    // 物品存在, 返回物品等级
    private fun getLevel(inputItem: NekoStack?): Double {
        return inputItem?.level?.toDouble() ?: .0
    }

    private fun getRarityNumber(inputItem: NekoStack?): Double {
        return inputItem?.rarity?.key?.let(table.rarityNumberMapping::get) ?: .0
    }

    private fun getPenalty(inputItem: NekoStack?): Double {
        return inputItem?.reforgeHistory?.modCount?.toDouble() ?: .0
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
    fun empty(player: Player): MergingSession.ReforgeResult {
        return Simple(false, MessageConstants.MSG_MERGING_RESULT_EMPTY.translate(player), ReforgeType.empty(player), ReforgeCost.zero(player), NekoStack.empty())
    }

    /**
     * 构建一个用于表示*合并失败*的 [MergingSession.ReforgeResult].
     */
    fun failure(player: Player, description: ComponentLike): MergingSession.ReforgeResult {
        return Simple(false, description.translate(player), ReforgeType.failure(player), ReforgeCost.failure(player), NekoStack.empty())
    }

    /**
     * 构建一个用于表示*合并成功*的 [MergingSession.ReforgeResult].
     */
    fun success(player: Player, item: NekoStack, type: MergingSession.ReforgeType, cost: MergingSession.ReforgeCost): MergingSession.ReforgeResult {
        return Simple(true, MessageConstants.MSG_MERGING_RESULT_SUCCESS.translate(player), type, cost, item)
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
    fun empty(viewer: Player): MergingSession.ReforgeType {
        return Empty(viewer)
    }

    /**
     * 构建一个用于表示合并失败的 [MergingSession.ReforgeType].
     */
    fun failure(viewer: Player): MergingSession.ReforgeType {
        return Failure(viewer)
    }

    /**
     * 通过 [AttributeModifier.Operation] 构建 [MergingSession.ReforgeType].
     */
    fun success(viewer: Player, operation: AttributeModifier.Operation): MergingSession.ReforgeType {
        return when (operation) {
            AttributeModifier.Operation.ADD -> Success0(viewer)
            AttributeModifier.Operation.MULTIPLY_BASE -> Success1(viewer)
            AttributeModifier.Operation.MULTIPLY_TOTAL -> Success2(viewer)
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

    private class Empty(viewer: Player) : Base() {
        override val operation: AttributeModifier.Operation
            get() = throw IllegalStateException("this type is not supposed to be used.")
        override val description: List<Component> = listOf(MessageConstants.MSG_MERGING_TYPE_EMPTY.translate(viewer))
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("operation", "n/a"),
            ExaminableProperty.of("description", description.plain),
        )
    }

    private class Failure(viewer: Player) : Base() {
        override val operation: AttributeModifier.Operation
            get() = throw IllegalStateException("this type is not supposed to be used.")
        override val description: List<Component> = listOf(MessageConstants.MSG_MERGING_TYPE_FAILURE.translate(viewer))
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("operation", "n/a"),
            ExaminableProperty.of("description", description.plain),
        )
    }

    private class Success0(viewer: Player) : Base() {
        override val operation: AttributeModifier.Operation = AttributeModifier.Operation.ADD
        override val description: List<Component> = listOf(MessageConstants.MSG_MERGING_TYPE_SUCCESS_0.translate(viewer))
    }

    private class Success1(viewer: Player) : Base() {
        override val operation: AttributeModifier.Operation = AttributeModifier.Operation.MULTIPLY_BASE
        override val description: List<Component> = listOf(MessageConstants.MSG_MERGING_TYPE_SUCCESS_1.translate(viewer))
    }

    private class Success2(viewer: Player) : Base() {
        override val operation: AttributeModifier.Operation = AttributeModifier.Operation.MULTIPLY_TOTAL
        override val description: List<Component> = listOf(MessageConstants.MSG_MERGING_TYPE_SUCCESS_2.translate(viewer))
    }
}

/**
 * 包含了构建各种 [MergingSession.ReforgeCost] 的方法.
 */
internal object ReforgeCost {
    /**
     * 表示没有资源消耗.
     */
    fun zero(viewer: Player): MergingSession.ReforgeCost {
        return Zero(viewer)
    }

    /**
     * 表示由于合并失败而产生的资源消耗.
     */
    fun failure(viewer: Player): MergingSession.ReforgeCost {
        return Failure(viewer)
    }

    /**
     * 表示由于合并成功而产生的资源消耗.
     */
    fun success(viewer: Player, defaultCurrencyAmount: Double): MergingSession.ReforgeCost {
        return Success(viewer, defaultCurrencyAmount)
    }

    private abstract class Base : MergingSession.ReforgeCost {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("description", description.plain),
        )

        override fun toString(): String = toSimpleString()
    }

    private class Zero(viewer: Player) : Base() {
        override fun take(viewer: Player) = Unit
        override fun test(viewer: Player): Boolean = true
        override val description: List<Component> = listOf(MessageConstants.MSG_MERGING_COST_ZERO.translate(viewer))
    }

    private class Failure(viewer: Player) : Base() {
        override fun take(viewer: Player): Unit = throw IllegalStateException("this cost is not supposed to be taken.")
        override fun test(viewer: Player): Boolean = throw IllegalStateException("this cost is not supposed to be tested.")
        override val description: List<Component> = listOf(MessageConstants.MSG_MERGING_COST_EMPTY.translate(viewer))
    }

    // 2024/8/12 TBD
    // 支持多货币
    // 支持自定义物品
    private class Success(
        viewer: Player,
        val currencyAmount: Double,
    ) : Base() {
        override fun take(viewer: Player) {
            EconomyManager.take(viewer.uniqueId, currencyAmount)
        }

        override fun test(viewer: Player): Boolean {
            return EconomyManager.has(viewer.uniqueId, currencyAmount).getOrDefault(false)
        }

        override val description: List<Component> = listOf(
            MessageConstants.MSG_MERGING_COST_SUCCESS.arguments(
                TranslationArgument.numeric(currencyAmount)
            ).translate(viewer)
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