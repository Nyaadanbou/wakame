package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.attribute.facade.BinaryAttributeValue
import cc.mewcraft.wakame.item.BinaryCoreValue

/**
 * 代表一个技能在 NBT 中的数据。
 *
 * 该接口的设计思想与 [BinaryAttributeValue] 十分相似。
 */
sealed interface BinaryAbilityValue : BinaryCoreValue

/**
 * 数据结构为 Map 的技能。
 */
data class BinaryAbilityValueMap(
    // FIXME 但话说回来，直接用 Compound 存是不是更好？还是说那样在性能上不如一个 ArrayMap 好
    //  或者，内置一个 Compound，然后提供封装好的函数来直接获取相应的数据
    //  或者，这里的 Any 改为更具体的类型而不是 Any，因为未来还需要支持 Molang
    val map: MutableMap<String, Any>,
) : BinaryAbilityValue {
    // 我考虑了一下，技能的数据结构多样性要比属性丰富太多了，不太适合枚举所有可能的数据结构
    // 但无论怎么变，依然在 key-value pair 这个形式之内
    // 因此使用 Map 来储存这些技能的数据足以覆盖全部情况
}