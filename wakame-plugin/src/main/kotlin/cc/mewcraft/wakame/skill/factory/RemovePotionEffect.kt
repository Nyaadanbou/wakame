package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.FixedSkillCastResult
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.SkillCastResult
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffectType

interface RemovePotionEffect : Skill {

    /**
     * 要移除的效果类型.
     */
    val effectTypes: List<PotionEffectType>

    companion object Factory : SkillFactory<RemovePotionEffect> {
        override fun create(key: Key, config: ConfigProvider): RemovePotionEffect {
            val effectTypes = config.optionalEntry<List<PotionEffectType>>("effect_types").orElse(emptyList())
            return DefaultImpl(key, config, effectTypes)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        effectTypes: Provider<List<PotionEffectType>>,
    ) : RemovePotionEffect, SkillBase(key, config) {

        override val effectTypes: List<PotionEffectType> by effectTypes

        override fun cast(context: SkillCastContext): SkillCastResult {
            val entity = context.optional(SkillCastContextKey.CASTER_ENTITY)?.bukkitEntity ?: return FixedSkillCastResult.NONE_CASTER
            if (entity is LivingEntity) {
                effectTypes.forEach { entity.removePotionEffect(it) }
            }
            return FixedSkillCastResult.SUCCESS
        }
    }
}