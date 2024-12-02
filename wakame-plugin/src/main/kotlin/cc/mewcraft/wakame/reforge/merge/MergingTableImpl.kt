package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.util.bindInstance
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.MochaFunction
import team.unnamed.mocha.runtime.binding.Binding
import xyz.xenondevs.commons.collections.enumMap
import java.util.stream.Stream

/**
 * 一个无限制的 [MergingTable] 实现.
 */
internal object WtfMergingTable : MergingTable {
    override val identifier: String = "wtf"
    override val enabled: Boolean = true
    override val title: Component = text("Merging Table (Cheat ON)")

    override val inputLevelLimit: Int
        // 无最大输出等级限制
        get() = Int.MAX_VALUE

    override val outputLevelLimit: Int
        // 无最大输出等级限制
        get() = Int.MAX_VALUE

    override val outputPenaltyLimit: Int
        // 无最大输出惩罚限制
        get() = Int.MAX_VALUE

    override val acceptableCoreMatcher: CoreMatchRuleContainer
        // 匹配任意核心
        get() = CoreMatchRuleContainer.any()

    override val rarityNumberMapping: RarityNumberMapping
        // 固定映射值
        get() = RarityNumberMapping.constant(10.0)

    override val valueMergeMethod: MergingTable.ValueMergeMethod = SimpleMergingTable.ValueMergeMethod(
        enumMap<MergingTable.ValueMergeMethod.Type, MergingTable.ValueMergeMethod.AlgorithmData>().withDefault { key ->
            MergingTable.ValueMergeMethod.AlgorithmData(
                // 返回两个值的和
                "query.value_1() + query.value_2()",
                spread = 0.2, min = -0.7, max = +0.7
            )
        }
    )

    override val levelMergeMethod: MergingTable.LevelMergeMethod = SimpleMergingTable.LevelMergeMethod(
        // 返回两个等级中的较大值
        "math.max ( query.level_1(), query.level_2() )"
    )

    override val penaltyMergeMethod: MergingTable.PenaltyMergeMethod = SimpleMergingTable.PenaltyMergeMethod(
        // 返回两个惩罚值之和再加一
        "query.penalty_1() + query.penalty_2() + 1"
    )

    override val totalCost: MergingTable.CurrencyCost = SimpleMergingTable.CurrencyCost(
        // 返回基础花费加上稀有度映射值之和
        "query.base() + query.rarity_1() + query.rarity_2()"
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 一个标准的 [MergingTable] 实现, 需要从配置文件构建.
 */
internal class SimpleMergingTable(
    override val identifier: String,
    override val enabled: Boolean,
    override val title: Component,
    override val inputLevelLimit: Int,
    override val outputLevelLimit: Int,
    override val outputPenaltyLimit: Int,
    override val acceptableCoreMatcher: CoreMatchRuleContainer,
    override val rarityNumberMapping: RarityNumberMapping,
    override val valueMergeMethod: MergingTable.ValueMergeMethod,
    override val levelMergeMethod: MergingTable.LevelMergeMethod,
    override val penaltyMergeMethod: MergingTable.PenaltyMergeMethod,
    override val totalCost: MergingTable.CurrencyCost,
) : MergingTable {

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("enabled", enabled),
        ExaminableProperty.of("title", title),
        ExaminableProperty.of("inputLevelLimit", inputLevelLimit),
        ExaminableProperty.of("outputLevelLimit", outputLevelLimit),
        ExaminableProperty.of("outputPenaltyLimit", outputPenaltyLimit),
        ExaminableProperty.of("acceptableCoreMatcher", acceptableCoreMatcher),
        ExaminableProperty.of("rarityNumberMapping", rarityNumberMapping),
        ExaminableProperty.of("valueMergeMethod", valueMergeMethod),
        ExaminableProperty.of("levelMergeMethod", levelMergeMethod),
        ExaminableProperty.of("penaltyMergeMethod", penaltyMergeMethod),
        ExaminableProperty.of("totalCost", totalCost),
    )

    override fun toString(): String = toSimpleString()

    @ConfigSerializable
    data class ValueMergeMethod(
        @Setting(nodeFromParent = true)
        private val methods: MutableMap<MergingTable.ValueMergeMethod.Type, MergingTable.ValueMergeMethod.AlgorithmData>,
    ) : MergingTable.ValueMergeMethod {
        override fun compile(type: MergingTable.ValueMergeMethod.Type, session: MergingSession): MergingTable.ValueMergeMethod.Algorithm {
            val data = methods[type] ?: throw IllegalArgumentException("no algorithm for type '$type'")
            val mocha = MochaEngine.createStandard().apply { bindInstance(NumberMergeBinding(session), "query") }
            val meanFunction = mocha.prepareEval(data.base)
            return MergingTable.ValueMergeMethod.Algorithm(data, meanFunction)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("methods", methods)
        )

        override fun toString(): String = toSimpleString()
    }

    @ConfigSerializable
    class LevelMergeMethod(
        @Setting(nodeFromParent = true)
        private val code: String,
    ) : MergingTable.LevelMergeMethod {

        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = OutputLevelBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code)
        )

        override fun toString(): String = toSimpleString()
    }

    @ConfigSerializable
    class PenaltyMergeMethod(
        @Setting(nodeFromParent = true)
        private val code: String,
    ) : MergingTable.PenaltyMergeMethod {

        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = OutputPenaltyBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code)
        )

        override fun toString(): String = toSimpleString()
    }

    @ConfigSerializable
    class CurrencyCost(
        @Setting(nodeFromParent = true)
        private val code: String,
    ) : MergingTable.CurrencyCost {
        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = CurrencyCostBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code),
        )

        override fun toString(): String = toSimpleString()
    }
}

//<editor-fold desc="Mocha bindings">
@Suppress("unused")
@Binding("query")
internal class NumberMergeBinding(
    val session: MergingSession,
) {
    @Binding("value_1")
    fun value1(): Double = session.getValue1()

    @Binding("value_2")
    fun value2(): Double = session.getValue2()
}

@Suppress("unused")
@Binding("query")
internal class OutputLevelBinding(
    val session: MergingSession,
) {
    @Binding("level_1")
    fun level1(): Double = session.getLevel1()

    @Binding("level_2")
    fun level2(): Double = session.getLevel2()
}

@Suppress("unused")
@Binding("query")
internal class OutputPenaltyBinding(
    val session: MergingSession,
) {
    @Binding("penalty_1")
    fun penalty1(): Double = session.getPenalty1()

    @Binding("penalty_2")
    fun penalty2(): Double = session.getPenalty2()
}

@Suppress("unused")
@Binding("query")
internal class CurrencyCostBinding(
    val session: MergingSession,
) {
    @Binding("level_1")
    fun level1(): Double = session.getLevel1()

    @Binding("level_2")
    fun level2(): Double = session.getLevel2()

    @Binding("rarity_1")
    fun rarity1(): Double = session.getRarityNumber1()

    @Binding("rarity_2")
    fun rarity2(): Double = session.getRarityNumber2()

    @Binding("penalty_1")
    fun penalty1(): Double = session.getPenalty1()

    @Binding("penalty_2")
    fun penalty2(): Double = session.getPenalty2()
}
//</editor-fold>