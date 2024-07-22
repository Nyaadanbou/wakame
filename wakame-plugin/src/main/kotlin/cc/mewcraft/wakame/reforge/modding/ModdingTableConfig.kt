package cc.mewcraft.wakame.reforge.modding

import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reforge.modding.match.CoreMatchRule
import cc.mewcraft.wakame.reforge.modding.match.CurseMatchRule
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * 封装了定制台的配置文件.
 */
interface ModdingTableConfig {
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
    val cost: StationCost

    /**
     * 针对具体的物品类型的定制规则.
     *
     * [Key] 为物品的类型 (萌芽物品的 id), [ItemRule] 为对应的定制规则.
     */
    val rules: Map<Key, ItemRule>
}

/**
 * 适用于一整个定制工作台的花费设置.
 *
 * 这里定义的所有成员的具体用途都取决于具体的实现.
 */
interface StationCost {
    /**
     * 基础花费.
     */
    val base: Double

    /**
     * 每个词条栏的花费.
     */
    val perCell: Double

    /**
     * 稀有度的花费系数.
     */
    val rarityModifiers: Map<Rarity, Double>

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
interface ItemRule {
    /**
     * 适用的萌芽物品类型.
     */
    val target: Key

    /**
     * 词条栏的定制规则.
     *
     * [String] 为词条栏的 id, [CellRule] 为对应的定制规则.
     */
    val cellRules: Map<String, CellRule>
}

/**
 * 代表一个词条栏的定制规则.
 */
interface CellRule {
    /**
     * 该规则适用的词条栏的 id.
     */
    val target: String

    /**
     * 定制词条栏所需要的权限.
     */
    val permission: String

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
    val acceptedCurse: List<CurseMatchRule>
}
