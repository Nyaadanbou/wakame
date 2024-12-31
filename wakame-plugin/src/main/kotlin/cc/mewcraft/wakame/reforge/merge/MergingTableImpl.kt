package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.util.bindInstance
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component.text
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.runtime.MochaFunction
import team.unnamed.mocha.runtime.binding.Binding
import java.util.stream.Stream

/**
 * 一个无限制的 [MergingTable] 实现.
 */
internal object WtfMergingTable : MergingTable {
    override val id: String = "wtf"
    override val primaryMenuSettings: BasicMenuSettings = BasicMenuSettings(
        title = text("Merging Table (Cheat Mode)"),
        structure = arrayOf(
            ". . . . . . . . .",
            ". . . . . . . . .",
            ". a . b . . . c .",
            ". . . . . . . . .",
            ". . . . . . . . .",
        ),
        icons = hashMapOf(
            "background" to Key.key("internal:menu/common/default/background"),
            "output_ok_confirmed" to Key.key("internal:menu/merging/default/output_ok_confirmed"),
            "output_ok_unconfirmed" to Key.key("internal:menu/merging/default/output_ok_unconfirmed"),
            "output_empty" to Key.key("internal:menu/merging/default/output_empty"),
            "output_failure" to Key.key("internal:menu/merging/default/output_failure"),
            "output_insufficient_resource" to Key.key("internal:menu/merging/default/output_insufficient_resource"),
        )
    )

    override val inputLevelLimit: Int
        get() = Int.MAX_VALUE // 无最大输出等级限制

    override val outputLevelLimit: Int
        get() = Int.MAX_VALUE // 无最大输出等级限制

    override val outputPenaltyLimit: Int
        get() = Int.MAX_VALUE // 无最大输出惩罚限制

    override val acceptableCoreMatcher: CoreMatchRuleContainer
        get() = CoreMatchRuleContainer.any() // 匹配任意核心

    override val rarityNumberMapping: RarityNumberMapping
        get() = RarityNumberMapping.constant(10.0) // 固定映射值

    override val valueMergeMethod: MergingTable.ValueMergeMethod = object : MergingTable.ValueMergeMethod {
        override fun compile(type: MergingTable.ValueMergeMethod.Type, session: MergingSession): MergingTable.ValueMergeMethod.Algorithm {
            // 返回两个值的和
            val data = MergingTable.ValueMergeMethod.AlgorithmData("query.value_1() + query.value_2()", spread = 0.2, min = -0.7, max = +0.7)
            // 其他按照一般流程
            val mocha = MochaEngine.createStandard().apply { bindInstance(NumberMergeBinding(session), "query") }
            val meanFunction = mocha.prepareEval(data.base)
            return MergingTable.ValueMergeMethod.Algorithm(data, meanFunction)
        }
    }

    override val levelMergeMethod: MergingTable.LevelMergeMethod = SimpleMergingTable.LevelMergeMethod(
        "math.max ( query.level_1(), query.level_2() )" // 返回两个等级中的较大值
    )

    override val penaltyMergeMethod: MergingTable.PenaltyMergeMethod = SimpleMergingTable.PenaltyMergeMethod(
        "query.penalty_1() + query.penalty_2() + 1" // 返回两个惩罚值之和再加一
    )

    override val totalCost: MergingTable.CurrencyCost = SimpleMergingTable.CurrencyCost(
        "query.base() + query.rarity_1() + query.rarity_2()" // 返回基础花费加上稀有度映射值之和
    )

    override fun toString(): String = toSimpleString()
}

/**
 * 一个标准的 [MergingTable] 实现, 需要从配置文件构建.
 */
internal class SimpleMergingTable(
    override val id: String,
    override val primaryMenuSettings: BasicMenuSettings,
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
        ExaminableProperty.of("id", id),
        ExaminableProperty.of("primaryMenuSettings", primaryMenuSettings),
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
    }

    @ConfigSerializable
    data class LevelMergeMethod(
        @Setting(nodeFromParent = true)
        private val code: String,
    ) : MergingTable.LevelMergeMethod {
        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = OutputLevelBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }
    }

    @ConfigSerializable
    data class PenaltyMergeMethod(
        @Setting(nodeFromParent = true)
        private val code: String,
    ) : MergingTable.PenaltyMergeMethod {
        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = OutputPenaltyBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }
    }

    @ConfigSerializable
    data class CurrencyCost(
        @Setting(nodeFromParent = true)
        private val code: String,
    ) : MergingTable.CurrencyCost {
        override fun compile(session: MergingSession): MochaFunction {
            val mocha = MochaEngine.createStandard()
            val query = CurrencyCostBinding(session)
            mocha.bindInstance(query, "query")
            return mocha.prepareEval(code)
        }
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