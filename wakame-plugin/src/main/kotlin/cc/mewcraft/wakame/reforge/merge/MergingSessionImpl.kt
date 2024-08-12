package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.attribute.AttributeComponent
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.NekoStackDelegates
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
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
) : MergingSession, KoinComponent {
    private val logger: Logger by inject()

    companion object {
        private val PREFIX = "[${SimpleMergingSession::class.simpleName}]"
    }

    constructor(
        viewer: Player,
        table: MergingTable,
        inputItem1: NekoStack?,
        inputItem2: NekoStack?,
    ) : this(viewer, table) {
        this.inputItem1 = inputItem1
        this.inputItem2 = inputItem2
    }

    override var inputItem1: NekoStack? by NekoStackDelegates.nullableCopyOnWrite(null)
    override var inputItem2: NekoStack? by NekoStackDelegates.nullableCopyOnWrite(null)

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
        }?.unsafe?.handle

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

    override var result: MergingSession.Result by Delegates.vetoable(Result.empty()) { _, old, new ->
        if (frozen) {
            logger.error("$PREFIX Trying to set result of a frozen merging session. This is a bug!")
            return@vetoable false
        }

        logger.info("$PREFIX Updating result: $new")
        return@vetoable true
    }

    private val _numberMergeFunction = mutableMapOf<MergingTable.NumberMergeFunction.Type, MochaFunction>()
    override val numberMergeFunction: (MergingTable.NumberMergeFunction.Type) -> MochaFunction = { type ->
        _numberMergeFunction.getOrPut(type) { table.numberMergeFunction.compile(type, this) }
    }
    override val outputLevelFunction: MochaFunction = table.outputLevelFunction.compile(this)
    override val outputPenaltyFunction: MochaFunction = table.outputPenaltyFunction.compile(this)
    override val currencyCostFunction: MochaFunction = table.currencyCost.totalFunction.compile(this)

    override fun merge(): MergingSession.Result {
        val operation = MergeOperation(this, logger)

        val result = try {
            operation.execute()
        } catch (e: MergingOperationException) {
            logger.info("$PREFIX Failed to merge: {}", e.message)
            Result.failure()
        } catch (e: Exception) {
            logger.error("$PREFIX An unknown error occurred while merging. This is a bug!", e)
            Result.failure()
        }

        return result.also { this.result = it }
    }

    override fun reset() {
        inputItem1 = null
        inputItem2 = null
        result = Result.empty()
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
        val value = (comp.wrapped as? AttributeComponent.Fixed<Double>)?.value ?: return .0
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
        val mappedValue = table.rarityNumberMapping.get(rarityKey)
        return mappedValue
    }

    private fun getPenalty(inputItem: NekoStack?): Double {
        val comp = inputItem?.components?.get(ItemComponentTypes.PORTABLE_CORE) ?: return .0
        val penalty = comp.penalty
        return penalty.toDouble()
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

    override fun toString(): String =
        toSimpleString()

    /**
     * 包含了构建各种 [MergingSession.Result] 的方法.
     */
    object Result {

        /**
         * 构建一个用于表示*空合并*的 [MergingSession.Result].
         */
        fun empty(): MergingSession.Result {
            return Result(false, NekoStack.empty(), Type.empty(), Cost.zero())
        }

        /**
         * 构建一个用于表示*合并失败*的 [MergingSession.Result].
         */
        fun failure(): MergingSession.Result {
            return Result(false, NekoStack.empty(), Type.failure(), Cost.failure())
        }

        /**
         * 构建一个用于表示*合并成功*的 [MergingSession.Result].
         */
        fun success(item: NekoStack, type: MergingSession.Type, cost: MergingSession.Cost): MergingSession.Result {
            return Result(true, item, type, cost)
        }

        /**
         * 一个一般的 [MergingSession.Result] 实现.
         */
        private class Result(
            successful: Boolean,
            item: NekoStack,
            type: MergingSession.Type,
            cost: MergingSession.Cost,
        ) : MergingSession.Result {

            override val successful = successful
            override val item: NekoStack by NekoStackDelegates.copyOnRead(item)
            override val type: MergingSession.Type = type
            override val cost = cost

            override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
                ExaminableProperty.of("successful", successful),
                ExaminableProperty.of("item", item),
                ExaminableProperty.of("type", type),
                ExaminableProperty.of("cost", cost),
            )

            override fun toString(): String =
                toSimpleString()
        }
    }

    /**
     * 包含了构建各种 [MergingSession.Type] 的方法.
     */
    object Type {

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
            override fun toString(): String {
                return toSimpleString()
            }
        }

        private class Empty : Base() {
            override val operation: AttributeModifier.Operation
                get() = throw IllegalStateException("This type is not supposed to be used.")
            override val description: List<Component> = listOf(
                text {
                    content("合并类型: 无")
                    color(NamedTextColor.WHITE)
                    decoration(TextDecoration.ITALIC, false)
                }
            )
        }

        private class Failure : Base() {
            override val operation: AttributeModifier.Operation
                get() = throw IllegalStateException("This type is not supposed to be used.")
            override val description: List<Component> = listOf(
                text {
                    content("合并类型: 失败")
                    color(NamedTextColor.RED)
                    decoration(TextDecoration.ITALIC, false)
                }
            )
        }

        private class Success0 : Base() {
            override val operation: AttributeModifier.Operation =
                AttributeModifier.Operation.ADD
            override val description: List<Component> = listOf(
                text {
                    content("合并类型: OP0")
                    color(NamedTextColor.WHITE)
                    decoration(TextDecoration.ITALIC, false)
                }
            )
        }

        private class Success1 : Base() {
            override val operation: AttributeModifier.Operation =
                AttributeModifier.Operation.MULTIPLY_BASE
            override val description: List<Component> = listOf(
                text {
                    content("合并类型: OP1")
                    color(NamedTextColor.WHITE)
                    decoration(TextDecoration.ITALIC, false)
                }
            )
        }

        private class Success2 : Base() {
            override val operation: AttributeModifier.Operation =
                AttributeModifier.Operation.MULTIPLY_TOTAL
            override val description: List<Component> = listOf(
                text {
                    content("合并类型: OP2")
                    color(NamedTextColor.WHITE)
                    decoration(TextDecoration.ITALIC, false)
                }
            )
        }
    }

    /**
     * 包含了构建各种 [MergingSession.Cost] 的方法.
     */
    object Cost {
        /**
         * 无消耗.
         */
        fun zero(): MergingSession.Cost {
            return Zero
        }

        /**
         * 无法计算的消耗, 用于合并失败.
         */
        fun failure(): MergingSession.Cost {
            return Failure
        }

        /**
         * 普通消耗, 用于合并成功.
         */
        fun success(defaultCurrencyAmount: Double): MergingSession.Cost {
            return Success(defaultCurrencyAmount)
        }

        private abstract class Base : MergingSession.Cost {
            override fun toString(): String = toSimpleString()
        }

        /**
         * 表示无资源消耗.
         */
        private data object Zero : Base() {
            override fun take(viewer: Player) = Unit
            override fun test(viewer: Player): Boolean = true
            override val description: List<Component> = listOf(
                text {
                    content("合并花费: 无")
                    color(NamedTextColor.WHITE)
                    decoration(TextDecoration.ITALIC, false)
                }
            )
        }

        /**
         * 表示由于合并失败而产生的无法计算的资源消耗.
         */
        private data object Failure : Base() {
            override fun take(viewer: Player): Unit =
                throw IllegalStateException("This cost is not supposed to be taken.")

            override fun test(viewer: Player): Boolean =
                throw IllegalStateException("This cost is not supposed to be tested.")

            override val description: List<Component> = listOf(
                text {
                    content("合并花费: 无法计算")
                    color(NamedTextColor.RED)
                    decoration(TextDecoration.ITALIC, false)
                }
            )
        }

        // 2024/8/12 TBD
        // 支持多货币
        // 支持自定义物品
        /**
         * 表示一个合并成功的资源消耗.
         */
        private data class Success(
            val defaultCurrencyAmount: Double,
        ) : Base() {
            override fun take(viewer: Player) {
                // TODO 实现 take, 还有下面的 test
            }

            override fun test(viewer: Player): Boolean {
                return true
            }

            override val description: List<Component> = listOf(
                text {
                    content("合并花费: $defaultCurrencyAmount")
                    color(NamedTextColor.WHITE)
                    decoration(TextDecoration.ITALIC, false)
                }
            )

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("defaultCurrencyAmount", defaultCurrencyAmount),
            )
        }
    }
}