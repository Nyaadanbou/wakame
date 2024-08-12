package cc.mewcraft.wakame.reforge.merge

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 代表一个合并台(的配置文件).
 *
 * 合并台用于将两个核心合并成一个.
 */
interface MergingTable : Examinable {

    val identifier: String

    val enabled: Boolean

    val title: Component

    /**
     * 工作台接收的物品, 可以拥有的最高等级.
     */
    val maxInputItemLevel: Int

    /**
     * 工作台输出的物品, 可以拥有的最高惩罚值.
     */
    val maxOutputItemPenalty: Int

    /**
     * 用于检查玩家输入的核心, 是否允许参与合并.
     */
    val acceptedCoreMatcher: CoreMatchRuleContainer

    /**
     * 稀有度到固定数值的映射表.
     */
    val rarityNumberMapping: RarityNumberMapping

    /**
     * 玩家进行合并操作所需要花费的货币资源.
     */
    val currencyCost: CurrencyCost

    /**
     * 具体见 [NumberMergeFunction].
     */
    val numberMergeFunction: NumberMergeFunction

    /**
     * 具体见 [OutputLevelFunction].
     */
    val outputLevelFunction: OutputLevelFunction

    /**
     * 具体见 [OutputPenaltyFunction].
     */
    val outputPenaltyFunction: OutputPenaltyFunction

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
    interface NumberMergeFunction : Examinable {
        /**
         * 获取指定的源代码.
         *
         * 源代码的大概样子如下:
         * ```
         * ( query.x + query.y ) * ( 0.7 + math.random(-0.5, 0.5) )
         * ```
         *
         * @throws IllegalArgumentException 如果指定的源代码不存在
         */
        fun code(type: Type): String

        /**
         * 编译指定类型 [type] 的自定义函数.
         */
        fun compile(type: Type, session: MergingSession): MochaFunction

        /**
         * 合并操作的类型.
         */
        enum class Type {
            OP0, OP1, OP2;

            companion object {
                @JvmStatic
                fun by(op: AttributeModifier.Operation): Type = when (op) {
                    AttributeModifier.Operation.ADD -> OP0
                    AttributeModifier.Operation.MULTIPLY_BASE -> OP1
                    AttributeModifier.Operation.MULTIPLY_TOTAL -> OP2
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
    interface OutputLevelFunction : Examinable {
        /**
         * 源代码.
         */
        val code: String

        /**
         * 编译自定义函数.
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
    interface OutputPenaltyFunction : Examinable {
        /**
         * 源代码.
         */
        val code: String

        /**
         * 编译自定义函数.
         */
        fun compile(session: MergingSession): MochaFunction
    }

    /**
     * 适用于整个合并台的货币花费设置.
     */
    interface CurrencyCost : Examinable {
        /**
         * 基础花费.
         *
         * 具体用途取决于具体的实现!
         */
        val base: Double

        /**
         * 总花费的自定义函数.
         */
        val totalFunction: TotalFunction

        /**
         * 封装了用于计算合并总花费的自定义函数.
         *
         * 函数的上下文:
         * ```
         * query.base()
         *   基础花费
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
        interface TotalFunction : Examinable {
            /**
             * 源代码.
             */
            val code: String

            /**
             * 编译自定义函数.
             */
            fun compile(session: MergingSession): MochaFunction
        }
    }
}