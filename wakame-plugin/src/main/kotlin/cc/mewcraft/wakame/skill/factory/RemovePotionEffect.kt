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
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffectType

interface RemovePotionEffect : Skill {
    val effectType: List<PotionEffectType>

    companion object Factory : SkillFactory<RemovePotionEffect> {
        override fun create(key: Key, config: ConfigProvider): RemovePotionEffect {
            val display = config.optionalEntry<SkillDisplay>("displays").orElse(EmptySkillDisplay)
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)
            val effectTypes = config.optionalEntry<List<PotionEffectType>>("effect_types").orElse(emptyList())

            return Default(key, display, conditions, effectTypes)
        }
    }

    private class Default(
        override val key: Key,
        display: Provider<SkillDisplay>,
        conditions: Provider<SkillConditionGroup>,
        effectType: Provider<List<PotionEffectType>>
    ) : RemovePotionEffect {
        override val displays: SkillDisplay by display
        override val conditions: SkillConditionGroup by conditions
        override val effectType: List<PotionEffectType> by effectType

        override fun cast(context: SkillCastContext): SkillCastResult {
            val entity = context.optional(SkillCastContextKeys.CASTER_ENTITY)?.bukkitEntity ?: return SkillCastResult.NONE_CASTER
            if (entity is LivingEntity) {
                effectType.forEach { entity.removePotionEffect(it) }
            }
            return SkillCastResult.SUCCESS
        }
    }
}