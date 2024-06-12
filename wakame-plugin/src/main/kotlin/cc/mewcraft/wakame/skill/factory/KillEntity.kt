package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.EmptySkillDisplay
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillCastResult
import cc.mewcraft.wakame.skill.SkillDisplay
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys
import net.kyori.adventure.key.Key

interface KillEntity : Skill {
    companion object Factory : SkillFactory<KillEntity> {
        override fun create(key: Key, config: ConfigProvider): KillEntity {
            val display = config.optionalEntry<SkillDisplay>("displays").orElse(EmptySkillDisplay)
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)

            return Default(key, display, conditions)
        }
    }

    private class Default(
        override val key: Key,
        display: Provider<SkillDisplay>,
        conditions: Provider<SkillConditionGroup>
    ) : KillEntity {
        override val displays: SkillDisplay by display
        override val conditions: SkillConditionGroup by conditions

        override fun cast(context: SkillCastContext): SkillCastResult {
            val entity = context.optional(SkillCastContextKeys.TARGET_LIVING_ENTITY)?.bukkitEntity ?: return SkillCastResult.NONE_TARGET
            entity.health = 0.0
            return SkillCastResult.SUCCESS
        }
    }
}