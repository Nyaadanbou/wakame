package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey
import net.kyori.adventure.key.Key

interface Dash : Skill {

    /**
     * 冲刺的距离.
     */
    val distance: Double

    companion object Factory : SkillFactory<Dash> {
        override fun create(key: Key, config: ConfigProvider): Dash {
            val distance = config.entry<Double>("distance")
            return DefaultImpl(key, config, distance)
        }
    }

    private class DefaultImpl(
        override val key: Key,
        config: ConfigProvider,
        distance: Provider<Double>,
    ) : Dash, SkillBase(key, config) {

        override val distance: Double by distance

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
                player.sendPlainMessage("冲刺的前摇摇摇摇")
                return TickResult.ALL_DONE
            }

            override fun tickBackswing(): TickResult {
                val player = context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                player.sendPlainMessage("冲刺的后摇摇摇摇摇摇摇")
                return TickResult.ALL_DONE
            }

            override fun tickCast(): TickResult {
                val player =
                    context.optional(SkillCastContextKey.CASTER_PLAYER)?.bukkitPlayer ?: return TickResult.INTERRUPT
                val direction = player.location.direction.normalize()
                val velocity = direction.multiply(distance)
                player.velocity = velocity
                return TickResult.ALL_DONE
            }
        }
    }
}