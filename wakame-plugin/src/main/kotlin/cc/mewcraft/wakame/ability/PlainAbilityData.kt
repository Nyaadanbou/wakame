package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.item.BinaryCoreData

/**
 * 代表一个技能在 NBT 中的数据。
 */
sealed interface PlainAbilityData : BinaryCoreData

/**
 * 此单例仅作为占位符代码。
 */
data object NoopPlainAbilityData : PlainAbilityData