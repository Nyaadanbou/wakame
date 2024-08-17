package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 代表一个定制台, 包含配置文件.
 */
interface ModdingTable : Examinable {
    /**
     * 本定制台的唯一标识.
     */
    val identifier: String

    /**
     * 是否启用本定制台.
     */
    val enabled: Boolean // 未定义, 只是留个接口

    /**
     * 本定制台的GUI容器标题.
     */
    val title: Component

    /**
     * 稀有度到数值的映射.
     */
    val rarityNumberMapping: RarityNumberMapping

    /**
     * 本定制台的货币花费设置.
     */
    val currencyCost: CurrencyCost<TableTotalFunction>

    /**
     * 储存了本定制台支持的所有物品的定制规则.
     */
    val itemRules: ItemRuleMap

    /**
     * 封装了某个物品的定制规则.
     */
    interface ItemRule : Examinable {
        /**
         * 本规则的目标萌芽物品的类型.
         */
        val target: Key

        /**
         * 该物品每个词条栏的定制规则.
         */
        val cellRules: CellRuleMap
    }

    /**
     * [ItemRule] 的集合.
     */
    interface ItemRuleMap : Examinable {
        /**
         * 获取指定物品 [key] 的定制规则.
         *
         * @param key 物品的类型
         */
        operator fun get(key: Key): ItemRule?

        /**
         * 检查是否包含指定物品的 [key].
         *
         * 返回 `true` 则说明 [key] 对应的物品支持定制.
         * 返回 `false` 则说明 [key] 对应的物品不支持定制.
         *
         * @param key 物品的类型
         */
        operator fun contains(key: Key): Boolean
    }

    /**
     * 代表一个词条栏的定制规则.
     */
    interface CellRule : Examinable {
        /**
         * 最多可以定制本词条栏多少次?
         */
        val modLimit: Int

        /**
         * 是否要求输入的核心的元素类型与被定制物品的元素类型是一致的?
         */
        val requireElementMatch: Boolean

        /**
         * 定制本词条栏所需要的货币.
         */
        val currencyCost: CurrencyCost<CellTotalFunction>

        /**
         * 定制本词条栏所需要的前置权限.
         */
        val permission: String?

        /**
         * 记录了哪些核心种类可以参与定制本词条栏.
         */
        val acceptedCores: CoreMatchRuleContainer

        companion object {
            fun empty(): CellRule = EmptyCellRule
        }
    }

    /**
     * 代表一个映射, 储存了各个词条栏的定制规则.
     */
    interface CellRuleMap : Examinable {
        operator fun get(key: String): CellRule?

        companion object {
            fun empty(): CellRuleMap = EmptyCellRuleMap
        }
    }

    /**
     * 关于货币花费的设置.
     */
    interface CurrencyCost<F> : Examinable {
        /**
         * 自定义函数, 用于计算货币数量.
         */
        val total: F
    }

    /**
     * 自定义函数.
     */
    fun interface TableTotalFunction : Examinable {
        /**
         * 依据给定的 [session] 编译自定义函数.
         */
        fun compile(session: ModdingSession): MochaFunction
    }

    /**
     * 自定义函数.
     */
    fun interface CellTotalFunction : Examinable {
        /**
         * 依据给定的 [session] 和 [replace] 编译自定义函数.
         */
        fun compile(
            session: ModdingSession,
            replace: ModdingSession.Replace
        ): MochaFunction
    }
}


/* Private */


private object EmptyCellRule : ModdingTable.CellRule {
    override val modLimit: Int = 0
    override val requireElementMatch: Boolean = false
    override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> = CurrencyCost
    override val permission: String? = null
    override val acceptedCores: CoreMatchRuleContainer = CoreMatchRuleContainer.empty()

    private object CurrencyCost : ModdingTable.CurrencyCost<ModdingTable.CellTotalFunction> {
        override val total = ModdingTable.CellTotalFunction { _, _ -> MochaFunction { .0 } }
    }
}

private object EmptyCellRuleMap : ModdingTable.CellRuleMap {
    override fun get(key: String): ModdingTable.CellRule? = null
}
