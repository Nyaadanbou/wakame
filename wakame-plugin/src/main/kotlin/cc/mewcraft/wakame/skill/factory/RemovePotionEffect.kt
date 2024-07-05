package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillBase
import cc.mewcraft.wakame.skill.TriggerConditions
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.skill.tick.TickResult
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

        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillContext): SkillTick {
            return Tick(context, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }

        private inner class Tick(
            context: SkillContext,
            override val interruptTriggers: TriggerConditions,
            override val forbiddenTriggers: TriggerConditions
        ) : AbstractPlayerSkillTick(this@DefaultImpl, context) {

            private var counter: Int = 0

            override fun tickCastPoint(): TickResult {
                val player = context.get(SkillContextKey.CASTER_PLAYER).bukkitPlayer
                player.sendPlainMessage("移除药水效果前摇")
                counter++
                return if (counter >= 20) TickResult.ALL_DONE else TickResult.CONTINUE_TICK
            }

            override fun tickBackswing(): TickResult {
                val player = context.optional(SkillContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("移除药水效果后摇")
                counter++
                return if (counter >= 60) TickResult.ALL_DONE else TickResult.CONTINUE_TICK
            }

            override fun tickCast(): TickResult {
                val entity = context.get(SkillContextKey.CASTER_ENTITY).bukkitEntity
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