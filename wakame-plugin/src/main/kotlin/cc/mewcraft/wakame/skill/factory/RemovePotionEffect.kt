package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.NoTargetException
import cc.mewcraft.wakame.skill.SkillDisplay
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.condition.EmptySkillConditionGroup
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffectType

interface RemovePotionEffect : Skill {
    val effectType: List<PotionEffectType>

    companion object Factory : SkillFactory<RemovePotionEffect> {
        override fun create(key: Key, config: ConfigProvider): RemovePotionEffect {
            val display = config.entry<SkillDisplay>("")
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
        override val display: SkillDisplay by display
        override val conditions: SkillConditionGroup by conditions
        override val effectType: List<PotionEffectType> by effectType

        override fun cast(context: SkillCastContext) {
            val target = context.target as? Target.LivingEntity ?: throw NoTargetException()
            val entity = target.bukkitEntity
            effectType.forEach { entity.removePotionEffect(it) }
        }
    }
}