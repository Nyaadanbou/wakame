package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
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

        override fun cast(context: SkillContext): SkillTick<Dash> {
            return DashTick(context, this, triggerConditionGetter.interruptTriggers, triggerConditionGetter.forbiddenTriggers)
        }
    }
}

private class DashTick(
    context: SkillContext,
    skill: Dash,
    override val interruptTriggers: Provider<TriggerConditions>,
    override val forbiddenTriggers: Provider<TriggerConditions>
) : AbstractPlayerSkillTick<Dash>(skill, context) {

    override fun tickCastPoint(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("冲刺的前摇摇摇摇")
        return TickResult.ALL_DONE
    }

    override fun tickBackswing(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        player.sendPlainMessage("冲刺的后摇摇摇摇摇摇摇")
        return TickResult.ALL_DONE
    }

    override fun tickCast(tickCount: Long): TickResult {
        val player = context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()?.bukkitPlayer ?: return TickResult.INTERRUPT
        val direction = player.location.direction.normalize()
        val velocity = direction.multiply(skill.distance)
        player.velocity = velocity
        return TickResult.ALL_DONE
    }
}