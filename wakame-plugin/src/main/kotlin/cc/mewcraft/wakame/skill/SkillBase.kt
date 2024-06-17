package cc.mewcraft.wakame.skill

import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import net.kyori.adventure.key.Key

/**
 * 包含 [Skill] 共同的实现.
 */
abstract class SkillBase(
    override val key: Key,
    config: ConfigProvider,
) : Skill {
    override val displays: SkillDisplay by config.optionalEntry<SkillDisplay>("displays").orElse(SkillDisplay.empty())
    override val conditions: SkillConditionGroup by config.optionalEntry<SkillConditionGroup>("conditions").orElse(SkillConditionGroup.empty())
}