package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.item.CoreData

/**
 * 代表一个技能的简单数据。
 */
sealed interface BinarySkillData : CoreData.Binary

/**
 * 此单例仅作为占位符代码，以后可能需要扩展。
 */
data object NoopBinarySkillData : BinarySkillData
