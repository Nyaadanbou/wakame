package cc.mewcraft.wakame.reforge.rerolling

import cc.mewcraft.wakame.molang.Evaluable
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable

/**
 * 代表一个重造台(的配置文件).
 */
interface RerollingTable {
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
     * 常量是一些固定的数值, 例如基础花费, 稀有度花费映射等. 将作为下面公式的输入值.
     *
     * 公式是一些动态的数值, 例如每个词条栏的花费计算方式, 整个物品最终花费的计算方式等.
     */
    interface Cost : Examinable {
        /* 常量 */

        /**
         * 基础花费.
         */
        val base: Double

        /**
         * 每个稀有度的花费.
         */
        val rarityMapping: Map<Key, Double>

        /* 公式 */

        /**
         * 物品上的每个词条栏的花费的计算方式.
         */
        val eachCostFormula: Evaluable<*>

        /**
         * 整个物品在被重造时, 最终花费的计算方式.
         */
        val totalCostFormula: Evaluable<*>
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
         * 词条栏最多能重造几次.
         */
        val modLimit: Int

        /**
         * 该词条栏的花费. 具体作用由实现定义.
         */
        val cost: Double
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
         * @param key 词条栏的名称
         */
        operator fun get(key: String): CellRule?
    }
}