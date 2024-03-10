package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.item.BinaryCoreData

/**
 * 代表一个技能的简单数据。
 */
sealed interface PlainAbilityData : BinaryCoreData

/**
 * 此单例仅作为占位符代码，以后可能需要扩展。
 */
data object NoopPlainAbilityData : PlainAbilityData