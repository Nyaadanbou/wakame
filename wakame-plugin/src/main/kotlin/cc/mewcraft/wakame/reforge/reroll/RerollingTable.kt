package cc.mewcraft.wakame.reforge.reroll

import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import cc.mewcraft.wakame.reforge.reroll.RerollingTable.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import team.unnamed.mocha.runtime.MochaFunction

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
     * 稀有度到数值的映射.
     */
    val rarityNumberMapping: RarityNumberMapping

    /**
     * 重造台的花费设置.
     */
    val currencyCost: TableCurrencyCost

    /**
     * 包含了每个物品的重造规则.
     */
    val itemRuleMap: ItemRuleMap

    /**
     * 代表一个物品的重造规则.
     */
    interface ItemRule : Examinable {
        /**
         * 该物品每个核孔的重造规则.
         */
        val cellRuleMap: CellRuleMap
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

        operator fun contains(key: Key): Boolean
    }

    /**
     * 代表一个核孔的重造规则.
     */
    interface CellRule : Examinable {
        /**
         * 核孔最多能重造几次.
         */
        val maxReroll: Int

        /**
         * 该核孔的货币花费.
         */
        val currencyCost: CellCurrencyCost

        companion object {
            fun empty(): CellRule = EmptyCellRule
        }
    }

    /**
     * 该对象本质是一个映射, 包含了所有核孔的重造规则.
     */
    interface CellRuleMap : Examinable {
        /**
         * 获取核孔的重造规则.
         *
         * 如果返回 `null` 则说明该核孔不支持重造.
         *
         * @param key 核孔的唯一标识
         */
        operator fun get(key: String): CellRule?

        operator fun contains(key: String): Boolean

        companion object {
            fun empty(): CellRuleMap = EmptyCellRuleMap
        }
    }

    //////

    /**
     * 可用上下文:
     * ```
     * query.source_rarity()
     *   no args
     * query.source_item_level()
     *   no args
     * query.cell_count(`type`)
     *   type = 'all' | 'selected' | 'unselected'
     * query.cell_cost(`type`)
     *   type = 'all' | 'selected' | 'unselected'
     * ```
     */
    fun interface TableCurrencyCost {
        fun compile(session: RerollingSession): MochaFunction
    }

    /**
     * 可用上下文:
     * ```
     * query.max_reroll()
     *   no args
     * query.reroll_count()
     *   no args
     * ```
     */
    fun interface CellCurrencyCost {
        fun compile(session: RerollingSession, selection: RerollingSession.Selection): MochaFunction
    }
}


/* Internals */


private object EmptyCellRule : CellRule {
    override val maxReroll: Int = 0
    override val currencyCost: CellCurrencyCost = CellCurrencyCost { _, _ -> MochaFunction { Double.NaN } }
}

private object EmptyCellRuleMap : CellRuleMap {
    override fun get(key: String): CellRule? = null
    override fun contains(key: String): Boolean = false
}