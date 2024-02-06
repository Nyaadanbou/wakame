package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.attribute.BinaryAttributeValue
import cc.mewcraft.wakame.item.BinaryCoreValue

/**
 * 代表一个技能在 NBT 中的数据。
 *
 * 该接口的设计思想与 [BinaryAttributeValue] 十分相似。
 */
sealed interface AbilityBinaryValue : BinaryCoreValue

/**
 * 数据结构为 Map 的技能。
 */
data class AbilityBinaryValueC(
    // FIXME 但话说回来，直接用 Compound 存是不是更好？还是说那样在性能上不如一个 ArrayMap 好
    val map: MutableMap<String, Any>,
) : AbilityBinaryValue {
    // 我考虑了一下，技能的数据结构多样性要比属性丰富太多了
    // 但无论怎么变，依然在 key-value pair 这个形式之内
    // 因此使用 Map 来储存这些技能的数据足以覆盖全部情况
}