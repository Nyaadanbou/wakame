package cc.mewcraft.wakame.reforge.reroll

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import team.unnamed.mocha.runtime.compiled.MochaCompiledFunction
import team.unnamed.mocha.runtime.compiled.Named

/**
 * 代表一个重造台(的配置文件).
 */
interface RerollingTable : Examinable {
    /**
     * 重造台的唯一标识.
     */
    val identifier: String

    /**
     * 重造台是否启用.
     */
    val enabled: Boolean

    /**
     * GUI 的标题.
     */
    val title: Component

    /**
     * 重造台的花费设置.
     */
    val cost: Cost

    /**
     * 包含了每个物品的重造规则.
     */
    val itemRules: ItemRuleMap

    /**
     * 重造台的花费设置.
     *
     * 分为两部分:
     * - 常量
     * - 计算方式
     *
     * 常量是*确切的数值*, 例如“基础花费”, 稀有度的数值映射等. 将作为下面公式的输入值.
     *
     * 计算方式是*数学表达式*, 例如每个词条栏的花费计算方式, 整个物品最终花费的计算方式等.
     */
    interface Cost : Examinable {
        /* 常量 */

        /**
         * 所谓的“基础花费”.
         */
        val base: Double

        /**
         * 每个稀有度对应的数值.
         */
        val rarityNumberMapping: Map<Key, Double>

        /* 计算方式 */

        /**
         * 物品上的每个词条栏的花费的计算方式.
         */
        val eachFunction: EachFunction

        /**
         * 整个物品在被重造时, 最终花费的计算方式.
         */
        val totalFunction: TotalFunction

        /**
         * 重造总花费的计算方式.
         */
        interface TotalFunction : MochaCompiledFunction {
            fun compute(
                @Named("base") base: Double,
                @Named("rarity") rarity: Double,
                @Named("item_level") itemLevel: Int,
                @Named("all_count") allCount: Int,
                @Named("selected_count") selectedCount: Int,
                @Named("selected_cost_sum") selectedCostSum: Double,
                @Named("unselected_cost_sum") unselectedCostSum: Double,
            ): Double
        }

        /**
         * 单个词条栏花费的计算方式.
         */
        interface EachFunction : MochaCompiledFunction {
            fun compute(
                @Named("cost") cost: Double,
                @Named("max_reroll") maxReroll: Int,
                @Named("reroll_count") rerollCount: Int, // 从 NBT 读取
            ): Double
        }
    }

    /**
     * 代表一个物品的重造规则.
     */
    interface ItemRule : Examinable {
        /**
         * 该物品每个词条栏的重造规则.
         */
        val cellRules: CellRuleMap
    }

    /**
     * 该对象本质是一个映射, 储存了各种物品的重造规则.
     */
    interface ItemRuleMap : Examinable {
        /**
         * 获取物品的重造规则.
         *
         * 如果返回 `null` 则说明该物品不支持重造.
         *
         * @param key 物品的类型
         */
        operator fun get(key: Key): ItemRule?
    }

    /**
     * 代表一个词条栏的重造规则.
     */
    interface CellRule : Examinable {
        /**
         * 该词条栏的花费. 具体作用看具体实现.
         */
        val cost: Double

        /**
         * 词条栏最多能重造几次.
         */
        val maxReroll: Int
    }

    /**
     * 该对象本质是一个映射, 包含了所有词条栏的重造规则.
     */
    interface CellRuleMap : Examinable {
        /**
         * 获取词条栏的重造规则.
         *
         * 如果返回 `null` 则说明该词条栏不支持重造.
         *
         * @param key 词条栏的唯一标识
         */
        operator fun get(key: String): CellRule?
    }
}