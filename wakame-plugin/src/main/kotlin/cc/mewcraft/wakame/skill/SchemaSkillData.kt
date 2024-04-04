package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.item.CoreData

/**
 * 代表一个技能的模板数据。
 */
sealed interface SchemaSkillData : CoreData.Schema

/**
 * 此单例仅作为占位符代码，以后可能需要扩展。
 */
data object NoopSchemaSkillData : SchemaSkillData
