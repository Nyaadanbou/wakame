package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.item.SchemaCoreData

/**
 * 代表一个技能的模板数据。
 */
sealed interface SchemaAbilityData : SchemaCoreData

/**
 * 此单例仅作为占位符代码，以后可能需要扩展。
 */
data object NoopSchemaAbilityData : SchemaAbilityData
