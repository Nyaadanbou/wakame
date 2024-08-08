package cc.mewcraft.wakame.reforge.merge

/**
 * 代表一个合并台(的配置文件).
 *
 * 合并台用于将两个核心合并成一个.
 */
interface MergingTable {
    val enabled: Boolean
    val title: String
    val cost: Cost
    val rules: ItemRuleMap

    /**
     * 适用于整个合并台的花费设置.
     *
     * 这里定义的所有成员的具体用途都取决于具体的实现.
     */
    interface Cost {
        /**
         * 基础花费.
         */
        val base: Double

        /**
         * 每个稀有度的花费.
         */
        val rarityMapping: Map<String, Double>
    }

    /**
     * 包含一个物品的合并规则.
     */
    interface ItemRule {
        // TODO 合并的具体流程是什么样的
    }

    /**
     * 该对象本质是一个映射, 包含了所有物品的合并规则.
     */
    interface ItemRuleMap {
        operator fun get(key: String): ItemRule?
    }
}