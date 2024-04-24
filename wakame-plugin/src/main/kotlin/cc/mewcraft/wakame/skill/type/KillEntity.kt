package cc.mewcraft.wakame.skill.type

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup

interface KillEntity : ConfiguredSkill {
    companion object Factory : SkillFactory<KillEntity> {
        override fun create(config: ConfigProvider): KillEntity {
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)

            return Default(conditions)
        }
    }

    private class Default(
        conditions: Provider<SkillConditionGroup>
    ) : KillEntity {
        override val conditions: SkillConditionGroup by conditions

        override fun cast(context: SkillCastContext) {
            val target = context.target as Target.LivingEntity
            target.bukkitEntity.health = 0.0
        }
    }
}