package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.item.SchemaCoreData

/**
 * 代表一个技能在模板中的数据。
 */
sealed interface SchemaAbilityData : SchemaCoreData

/**
 * 此单例仅为占位符代码。
 */
data object NoopSchemaAbilityData : SchemaAbilityData
