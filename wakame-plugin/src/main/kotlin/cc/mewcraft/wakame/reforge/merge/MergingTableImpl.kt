package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.util.bindInstance
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.examination.ExaminableProperty
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.MochaFunction
import team.unnamed.mocha.runtime.binding.Binding
import java.util.stream.Stream

/**
 * 一个无限制的 [MergingTable] 实现.
 */
internal object WtfMergingTable : MergingTable {
    override val identifier: String = "wtf"
    override val enabled: Boolean = true
    override val title: Component = text("Merging Table (Cheat ON)")

    override val maxInputItemLevel: Int =
        // 无最大输出等级限制
        Int.MAX_VALUE

    override val maxOutputItemPenalty: Int =
        // 无最大输出惩罚限制
        Int.MAX_VALUE

    override val acceptedCoreMatcher: CoreMatchRuleContainer = object : CoreMatchRuleContainer {
        // 匹配任意核心
        override fun test(core: Core): Boolean = true
    }

    override val rarityNumberMapping: RarityNumberMapping = object : RarityNumberMapping {
        // 永远返回 10.0
        override fun get(key: Key): Double = 10.0
    }

    override val numberMergeFunction: MergingTable.NumberMergeFunction = SimpleMergingTable.NumberMergeFunction(MergingTable.NumberMergeFunction.Type.entries.associateWith {
        // 返回两个属性数值之和
        "query.value_1() + query.value_2()"
    })

    override val outputLevelFunction: MergingTable.OutputLevelFunction = SimpleMergingTable.OutputLevelFunction(
        // 返回两个等级中的较大值
        "math.max ( query.level_1(), query.level_2() )"
    )

    override val outputPenaltyFunction: MergingTable.OutputPenaltyFunction = SimpleMergingTable.OutputPenaltyFunction(
        // 返回两个惩罚值之和再加一
        "query.penalty_1() + query.penalty_2() + 1"
    )

    override val currencyCost: MergingTable.CurrencyCost = SimpleMergingTable.CurrencyCost(
        0.0,
        SimpleMergingTable.CurrencyCost.TotalFunction(
            // 返回基础花费加上稀有度映射值之和
            "query.base() + query.rarity_1() + query.rarity_2()"
        )
    )
}

/**
 * 一个标准的 [MergingTable] 实现, 需要从配置文件构建.
 */
internal class SimpleMergingTable(
    override val identifier: String,
    override val enabled: Boolean,
    override val title: Component,
    override val maxInputItemLevel: Int,
    override val maxOutputItemPenalty: Int,
    override val acceptedCoreMatcher: CoreMatchRuleContainer,
    override val rarityNumberMapping: RarityNumberMapping,
    override val currencyCost: MergingTable.CurrencyCost,
    override val numberMergeFunction: MergingTable.NumberMergeFunction,
    override val outputLevelFunction: MergingTable.OutputLevelFunction,
    override val outputPenaltyFunction: MergingTable.OutputPenaltyFunction,
) : MergingTable {

    override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
        ExaminableProperty.of("enabled", enabled),
        ExaminableProperty.of("title", title),
        ExaminableProperty.of("maxInputLevel", maxInputItemLevel),
        ExaminableProperty.of("maxOutputPenalty", maxOutputItemPenalty),
        ExaminableProperty.of("coreMatcherMap", acceptedCoreMatcher),
        ExaminableProperty.of("rarityNumberMap", rarityNumberMapping),
        ExaminableProperty.of("numberMergeFunction", numberMergeFunction),
        ExaminableProperty.of("outputLevelFunction", outputLevelFunction),
        ExaminableProperty.of("outputPenaltyFunction", outputPenaltyFunction),
        ExaminableProperty.of("cost", currencyCost),
    )

    override fun toString(): String =
        toSimpleString()

    class NumberMergeFunction(
        private val code: Map<MergingTable.NumberMergeFunction.Type, String>,
    ) : MergingTable.NumberMergeFunction {
        override fun code(type: MergingTable.NumberMergeFunction.Type): String {
            return code[type] ?: throw IllegalArgumentException("No code for type '$type'")
        }

        override fun compile(type: MergingTable.NumberMergeFunction.Type, session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val code = code(type)
            val query = NumberMergeBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code)
        )

        override fun toString(): String =
            toSimpleString()
    }

    class OutputLevelFunction(
        override val code: String,
    ) : MergingTable.OutputLevelFunction {

        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = OutputLevelBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code)
        )

        override fun toString(): String =
            toSimpleString()
    }

    class OutputPenaltyFunction(
        override val code: String,
    ) : MergingTable.OutputPenaltyFunction {

        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = OutputPenaltyBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }

        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("code", code)
        )

        override fun toString(): String =
            toSimpleString()
    }

    class CurrencyCost(
        override val base: Double = 0.0,
        override val totalFunction: MergingTable.CurrencyCost.TotalFunction,
    ) : MergingTable.CurrencyCost {
        override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
            ExaminableProperty.of("base", base),
            ExaminableProperty.of("totalCostFunction", totalFunction),
        )

        override fun toString(): String =
            toSimpleString()


        class TotalFunction(
            override val code: String,
        ) : MergingTable.CurrencyCost.TotalFunction {

            override fun compile(session: MergingSession): MochaFunction {
                val mocha = MochaEngine.createStandard()
                val query = TotalCostBinding(session)
                mocha.bindInstance(query, "query")
                return mocha.prepareEval(code)
            }

            override fun examinableProperties(): Stream<out ExaminableProperty?> = Stream.of(
                ExaminableProperty.of("code", code)
            )

            override fun toString(): String =
                toSimpleString()
        }
    }
}

//<editor-fold desc="Mocha bindings">
@Binding("query")
internal class NumberMergeBinding(
    val session: MergingSession,
) {
    @Binding("value_1")
    fun value1(): Double = session.getValue1()

    @Binding("value_2")
    fun value2(): Double = session.getValue2()
}

@Binding("query")
internal class OutputLevelBinding(
    val session: MergingSession,
) {
    @Binding("level_1")
    fun level1(): Double = session.getLevel1()

    @Binding("level_2")
    fun level2(): Double = session.getLevel2()
}

@Binding("query")
internal class OutputPenaltyBinding(
    val session: MergingSession,
) {
    @Binding("penalty_1")
    fun penalty1(): Double = session.getPenalty1()

    @Binding("penalty_2")
    fun penalty2(): Double = session.getPenalty2()
}

@Binding("query")
internal class TotalCostBinding(
    val session: MergingSession,
) {
    @Binding("base")
    fun base(): Double = session.table.currencyCost.base

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