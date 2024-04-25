package cc.mewcraft.wakame.skill.type

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.NoTargetException
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import org.bukkit.potion.PotionEffectType

interface RemovePotionEffect : ConfiguredSkill {
    val effectType: List<PotionEffectType>

    companion object Factory : SkillFactory<RemovePotionEffect> {
        override fun create(config: ConfigProvider): RemovePotionEffect {
            val conditions = config.optionalEntry<SkillConditionGroup>("conditions").orElse(EmptySkillConditionGroup)
            val effectTypes = config.optionalEntry<List<PotionEffectType>>("effect_types").orElse(emptyList())

            return Default(conditions, effectTypes)
        }
    }

    private class Default(
        conditions: Provider<SkillConditionGroup>,
        effectType: Provider<List<PotionEffectType>>
    ) : RemovePotionEffect {
        override val conditions: SkillConditionGroup by conditions
        override val effectType: List<PotionEffectType> by effectType

        override fun cast(context: SkillCastContext) {
            val target = context.target as? Target.LivingEntity ?: throw NoTargetException()
            val entity = target.bukkitEntity
            effectType.forEach { entity.removePotionEffect(it) }
        }
    }
}