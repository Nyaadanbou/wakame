package cc.mewcraft.wakame.reforge.mod

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable

/**
 * 代表一个定制台, 包含配置文件.
 */
interface ModdingTable : Examinable {
    /**
     * 是否启用这个定制台.
     */
    val enabled: Boolean // 未定义, 只是留个接口

    /**
     * 定制台的GUI标题.
     */
    val title: Component

    /**
     * 定制台的花费设置, 该设置适用于整个定制台.
     */
    val cost: Cost

    /**
     * 针对具体的物品类型的定制规则.
     *
     * [Key] 为物品的类型 (萌芽物品的 id), [ItemRule] 为对应的定制规则.
     */
    val itemRules: ItemRuleMap

    /**
     * 适用于一整个定制工作台的花费设置.
     *
     * 这里定义的所有成员的具体用途都取决于具体的实现.
     */
    interface Cost : Examinable {
        /**
         * 基础花费.
         */
        val base: Double

        /**
         * 每个核心的花费.
         */
        val perCore: Double

        /**
         * 每个诅咒的花费.
         */
        val perCurse: Double

        /**
         * 稀有度的花费系数.
         */
        val rarityModifiers: Map<Key, Double>

        /**
         * 物品等级的花费系数.
         */
        val itemLevelModifier: Double

        /**
         * 核心等级的花费系数.
         */
        val coreLevelModifier: Double
    }

    /**
     * 代表一个物品的定制规则.
     */
    interface ItemRule : Examinable {
        /**
         * 适用的萌芽物品类型.
         */
        val target: Key

        /**
         * 该物品每个词条栏的定制规则.
         */
        val cellRules: CellRuleMap
    }

    /**
     * 代表一个映射, 储存了各种物品的定制规则.
     */
    interface ItemRuleMap : Examinable {
        /**
         * 获取指定物品的定制规则.
         *
         * 如果返回 `null` 则说明该物品不支持定制.
         *
         * @param key 物品的类型
         */
        operator fun get(key: Key): ItemRule?
    }

    /**
     * 代表一个词条栏的定制规则.
     */
    interface CellRule : Examinable {
        /**
         * 定制词条栏所需要的权限.
         */
        val permission: String?

        /**
         * 定制该词条栏的花费.
         */
        val cost: Double

        /**
         * 词条栏最多能定制几次.
         */
        val modLimit: Int

        /**
         * 词条栏“接受“哪些核心.
         *
         * 储存了具体的规则, 定义了一个核心必须满足什么样的规则才算被“接受”.
         */
        val acceptedCores: List<CoreMatchRule>

        /**
         * 词条栏“接受”哪些诅咒.
         *
         * 储存了具体的规则, 定义了一个诅咒必须满足什么样的规则才算被“接受”.
         */
        val acceptedCurses: List<CurseMatchRule>

        /**
         * 是否需要判断定制此词条栏的物品元素与输入物品的元素符合.
         */
        val requireElementMatch: Boolean
    }

    /**
     * 代表一个映射, 储存了各个词条栏的定制规则.
     */
    interface CellRuleMap : Examinable {
        operator fun get(key: String): CellRule?
    }
}
