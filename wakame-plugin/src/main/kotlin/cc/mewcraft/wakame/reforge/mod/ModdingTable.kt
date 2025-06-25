package cc.mewcraft.wakame.reforge.mod

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.reforge.common.CoreMatchRuleContainer
import cc.mewcraft.wakame.reforge.common.RarityNumberMapping
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import team.unnamed.mocha.runtime.MochaFunction

/**
 * 代表一个定制台, 包含配置文件.
 */
interface ModdingTable : Examinable {
    /**
     * 本定制台的唯一标识.
     */
    val id: String

    /**
     * 主要的基础菜单设置.
     */
    val primaryMenuSettings: BasicMenuSettings

    /**
     * 核孔的基础菜单设置.
     */
    val replaceMenuSettings: BasicMenuSettings

    /**
     * @see ReforgeCountAddMethod
     */
    val reforgeCountAddMethod: ReforgeCountAddMethod

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
    val itemRuleMap: ItemRuleMap

    /**
     * 物品的重铸次数的计算方式.
     */
    enum class ReforgeCountAddMethod {
        PLUS_ONE,
        ALL_CORE
    }

    /**
     * 封装了一个物品的定制规则.
     */
    interface ItemRule : Examinable {

        companion object {
            fun empty(): ItemRule = EmptyItemRule
        }

        /**
         * 目标萌芽物品的 id.
         */
        val itemId: Key

        /**
         * 该物品最多可以被定制的次数.
         */
        val modLimit: Int

        /**
         * 该物品每个核孔的定制规则.
         */
        val coreRuleMap: CoreRuleMap
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
     * 代表一个核孔的定制规则.
     */
    interface CoreContainerRule : Examinable {

        companion object {
            fun empty(): CoreContainerRule = EmptyCoreContainerRule
        }

        /**
         * 是否要求输入的核心的元素类型与被定制物品的元素类型是一致的?
         */
        val requireElementMatch: Boolean

        /**
         * 定制本核孔所需要的货币.
         */
        val currencyCost: CurrencyCost<CoreContainerTotalFunction>

        /**
         * 定制本核孔所需要的前置权限.
         */
        val permission: String?

        /**
         * 记录了哪些核心种类可以参与定制本核孔.
         */
        val acceptableCores: CoreMatchRuleContainer
    }

    /**
     * 储存了每个核孔的定制规则.
     */
    interface CoreRuleMap : Examinable {

        companion object {
            fun empty(): CoreRuleMap = EmptyCoreRuleMap
        }

        /**
         * 关于核孔 id 的 [Comparator], 基于核孔在配置文件中的顺序.
         */
        val comparator: Comparator<String?>

        /**
         * 获取指定 [key] 的核孔定制规则.
         *
         * 如果返回的不是 `null`, 则我们说这个核孔在定制系统中存在定义.
         * 否则, 我们说这个核孔在定制系统中不存在定义 (无法进行定制).
         */
        operator fun get(key: String): CoreContainerRule?
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
    fun interface CoreContainerTotalFunction : Examinable {
        /**
         * 依据给定的 [session] 和 [replace] 编译自定义函数.
         */
        fun compile(
            session: ModdingSession,
            replace: ModdingSession.Replace,
        ): MochaFunction
    }
}


/* Private */


private object EmptyItemRule : ModdingTable.ItemRule {
    override val itemId: Key
        get() = GenericKeys.EMPTY
    override val modLimit: Int
        get() = 0
    override val coreRuleMap: ModdingTable.CoreRuleMap
        get() = EmptyCoreRuleMap
}

private object EmptyCoreContainerRule : ModdingTable.CoreContainerRule {
    override val requireElementMatch: Boolean = false
    override val currencyCost: ModdingTable.CurrencyCost<ModdingTable.CoreContainerTotalFunction> = CurrencyCost
    override val permission: String? = null
    override val acceptableCores: CoreMatchRuleContainer = CoreMatchRuleContainer.empty()

    private object CurrencyCost : ModdingTable.CurrencyCost<ModdingTable.CoreContainerTotalFunction> {
        override val total = ModdingTable.CoreContainerTotalFunction { _, _ -> MochaFunction { .0 } }
    }
}

private object EmptyCoreRuleMap : ModdingTable.CoreRuleMap {
    override val comparator: Comparator<String?> = nullsLast(naturalOrder())
    override fun get(key: String): ModdingTable.CoreContainerRule? = null
}
