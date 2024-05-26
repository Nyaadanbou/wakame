package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import net.kyori.adventure.key.Key

interface KillEntity : Skill {
    companion object Factory : SkillFactory<KillEntity> {
        override fun create(key: Key, config: ConfigProvider): KillEntity {
            val display = config.optionalEntry<SkillDisplay>("").orElse(EmptySkillDisplay)
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)

            return Default(key, display, conditions)
        }
    }

    private class Default(
        override val key: Key,
        display: Provider<SkillDisplay>,
        conditions: Provider<SkillConditionGroup>
    ) : KillEntity {
        override val display: SkillDisplay by display
        override val conditions: SkillConditionGroup by conditions

        override fun cast(context: SkillCastContext) {
            val target = context.target as? Target.LivingEntity ?: throw NoTargetException()
            target.bukkitEntity.health = 0.0
        }
    }
}