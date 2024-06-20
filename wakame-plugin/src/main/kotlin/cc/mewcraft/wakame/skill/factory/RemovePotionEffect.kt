package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
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

        override fun cast(context: SkillCastContext): SkillTick {
            return Tick(context)
        }

        private inner class Tick(
            context: SkillCastContext,
        ) : PlayerSkillTick(this@DefaultImpl, context) {

            private var counter: Int = 0

            override fun tickCastPoint(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("移除药水效果前摇")
                counter++
                return if (counter >= 20) TickResult.ALL_DONE else TickResult.CONTINUE_TICK
            }

            override fun tickBackswing(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("移除药水效果后摇")
                counter++
                return if (counter >= 60) TickResult.ALL_DONE else TickResult.CONTINUE_TICK
            }

            override fun tickCast(): TickResult {
                val entity = context.optional(SkillCastContextKey.CASTER_ENTITY)?.bukkitEntity ?: return TickResult.INTERRUPT
                if (entity is LivingEntity) {
                    effectTypes.forEach { entity.removePotionEffect(it) }
                }
                entity.sendPlainMessage("正在移除药水效果...")
                counter++
                return if (counter >= 40) TickResult.ALL_DONE else TickResult.CONTINUE_TICK
            }
        }
    }
}