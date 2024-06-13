package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import net.kyori.adventure.key.Key

interface Dash : Skill {
    val distance: Double

    companion object Factory: SkillFactory<Dash> {
        override fun create(key: Key, config: ConfigProvider): Dash {
            val display = config.optionalEntry<SkillDisplay>("displays").orElse(EmptySkillDisplay)
            val distance = config.entry<Double>("distance")
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)

            return Default(key, display, distance, conditions)
        }
    }

    private class Default(
        override val key: Key,
        display: Provider<SkillDisplay>,
        distance: Provider<Double>,
        conditions: Provider<SkillConditionGroup>,
    ) : Dash {
        override val displays: SkillDisplay by display
        override val distance: Double by distance
        override val conditions: SkillConditionGroup by conditions

        override fun cast(context: SkillCastContext): SkillCastResult {
            val player = context.optional(SkillCastContextKeys.CASTER_PLAYER)?.bukkitPlayer ?: return FixedSkillCastResult.NONE_CASTER
            val direction = player.location.direction.normalize()
            val velocity = direction.multiply(distance)
            player.velocity = velocity
            return FixedSkillCastResult.SUCCESS
        }
    }
}