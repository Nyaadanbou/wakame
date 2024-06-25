package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import net.kyori.adventure.key.Key

interface KillEntity : Skill {
    companion object Factory : SkillFactory<KillEntity> {
        override fun create(key: Key, config: ConfigProvider): KillEntity {
            return DefaultImpl(key, config)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
    ) : KillEntity, SkillBase(key, config) {
        private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

        override fun cast(context: SkillCastContext): SkillTick {
            return Tick(context, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }

        private inner class Tick(
            context: SkillCastContext,
            override val interruptTriggers: TriggerConditions,
            override val forbiddenTriggers: TriggerConditions
        ) : PlayerSkillTick(this@DefaultImpl, context) {

            override fun tickCastPoint(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("杀死生物前摇awa")
                return TickResult.ALL_DONE
            }

            override fun tickBackswing(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("杀死生物后摇qwq")
                return TickResult.ALL_DONE
            }

            override fun tickCast(): TickResult {
                val entity = context.optional(SkillCastContextKey.TARGET_LIVING_ENTITY)?.bukkitEntity ?: return TickResult.INTERRUPT
                entity.health = 0.0
                return TickResult.ALL_DONE
            }
        }
    }
}