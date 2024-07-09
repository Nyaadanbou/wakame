package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
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

        override fun cast(context: SkillContext): SkillTick {
            return Tick(context, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }

        private inner class Tick(
            context: SkillContext,
            override val interruptTriggers: TriggerConditions,
            override val forbiddenTriggers: TriggerConditions
        ) : AbstractPlayerSkillTick(this@DefaultImpl, context) {

            override fun tickCastPoint(): TickResult {
                val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("杀死生物前摇awa")
                return TickResult.ALL_DONE
            }

            override fun tickBackswing(): TickResult {
                val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("杀死生物后摇qwq")
                return TickResult.ALL_DONE
            }

            override fun tickCast(): TickResult {
                val entity = context[SkillContextKey.TARGET]?.value<Target.LivingEntity>()?.bukkitEntity ?: return TickResult.INTERRUPT
                entity.health = 0.0
                return TickResult.ALL_DONE
            }
        }
    }
}