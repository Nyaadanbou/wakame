package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.util.RandomizedValue
import net.kyori.examination.Examinable
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 代表一个合并台(的配置文件).
 *
 * 合并台用于将两个核心合并成一个.
 */
interface MergingTable : Examinable {

    /**
     * 合并台的唯一标识符.
     * 用于指令和配置文件引用.
     */
    val id: String

    /**
     * 合并台的基础菜单设置.
     */
    val primaryMenuSettings: BasicMenuSettings

    /**
     * 工作台接收的物品可以拥有的最高等级.
     */
    val inputLevelLimit: Int

    /**
     * 工作台输出的物品可以拥有的最高等级.
     */
    val outputLevelLimit: Int

    /**
     * 工作台输出的物品可以拥有的最高惩罚值.
     */
    val outputPenaltyLimit: Int

    /**
     * 用于检查玩家输入的核心, 是否允许参与合并.
     */
    val acceptableCoreMatcher: CoreMatchRuleContainer

    /**
     * 稀有度到固定数值的映射表.
     */
    val rarityNumberMapping: RarityNumberMapping

    /**
     * @see ValueMergeMethod
     */
    val valueMergeMethod: ValueMergeMethod

    /**
     * @see LevelMergeMethod
     */
    val levelMergeMethod: LevelMergeMethod

    /**
     * @see PenaltyMergeMethod
     */
    val penaltyMergeMethod: PenaltyMergeMethod

    /**
     * 完成合并所需要的货币花费.
     */
    val totalCost: CurrencyCost

    /**
     * 封装了用于合并属性数值的自定义函数.
     *
     * 函数的上下文:
     * ```
     * query.value_1()
     *   第一个属性修饰符的数值
     * query.value_2()
     *   第二个属性修饰符的数值
     * ```
     */
    interface ValueMergeMethod {
        fun compile(type: Type, session: MergingSession): Algorithm

        fun compile(type: AttributeModifier.Operation, session: MergingSession): Algorithm {
            return compile(Type.by(type), session)
        }

        data class Algorithm(
            private val algorithmData: AlgorithmData, private val meanFunction: MochaFunction,
        ) {
            fun evaluate(): RandomizedValue.Result {
                val base = meanFunction.evaluate()
                val (_, spread, min, max) = algorithmData
                val rand = RandomizedValue(base, .0, spread, min, max)
                return rand.calculate()
            }
        }

        @ConfigSerializable
        data class AlgorithmData(
            val base: String, val spread: Double, val min: Double, val max: Double,
        )

        enum class Type(val operation: AttributeModifier.Operation) {
            ADD_VALUE(AttributeModifier.Operation.ADD),
            ADD_MULTIPLIED_BASE(AttributeModifier.Operation.MULTIPLY_BASE),
            ADD_MULTIPLIED_TOTAL(AttributeModifier.Operation.MULTIPLY_TOTAL);

            companion object {
                fun by(op: AttributeModifier.Operation): Type = when (op) {
                    AttributeModifier.Operation.ADD -> ADD_VALUE
                    AttributeModifier.Operation.MULTIPLY_BASE -> ADD_MULTIPLIED_BASE
                    AttributeModifier.Operation.MULTIPLY_TOTAL -> ADD_MULTIPLIED_TOTAL
                }
            }
        }
    }

    /**
     * 封装了用于计算输出核心等级的自定义函数.
     *
     * 函数的上下文:
     * ```
     * query.level_1()
     *   第一个核心的等级
     * query.level_2()
     *   第二个核心的等级
     * ```
     */
    interface LevelMergeMethod {
        /**
         * 编译函数.
         */
        fun compile(session: MergingSession): MochaFunction
    }

    /**
     * 封装了用于计算输出核心惩罚值的自定义函数.
     *
     * 函数的上下文:
     * ```
     * query.penalty_1()
     *   第一个核心的惩罚值
     * query.penalty_2()
     *   第二个核心的惩罚值
     * ```
     */
    interface PenaltyMergeMethod {
        /**
         * 编译函数.
         */
        fun compile(session: MergingSession): MochaFunction
    }

    /**
     * 封装了一般的货币花费.
     *
     * 函数的上下文:
     * ```
     * query.level_1()
     *   第一个物品的等级
     * query.level_2()
     *   第二个物品的等级
     * query.rarity_1()
     *   第一个物品的稀有度对应的数值
     * query.rarity_2()
     *   第二个物品的稀有度对应的数值
     * query.penalty_1()
     *   第一个物品的惩罚值
     * query.penalty_2()
     *   第二个物品的惩罚值
     * ```
     */
    interface CurrencyCost {
        /**
         * 编译函数.
         */
        fun compile(session: MergingSession): MochaFunction
    }
}