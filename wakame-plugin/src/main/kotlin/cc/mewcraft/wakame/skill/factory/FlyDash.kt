package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.Ticker
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface FlyDash : Skill {
    val distance: Double

    val beforeMovingEffects: List<Skill>

    companion object Factory : SkillFactory<FlyDash> {
        override fun create(key: Key, config: ConfigProvider): FlyDash {
            val distance = config.entry<Double>("distance")
            val castPointEffects = config.optionalEntry<List<Skill>>("before_moving_effects").orElse(emptyList())
            return FlyDashDefaultImpl(key, config, distance, castPointEffects)
        }
    }
}

private class FlyDashDefaultImpl(
    key: Key,
    config: ConfigProvider,
    distance: Provider<Double>,
    castPointEffects: Provider<List<Skill>>
): FlyDash, SkillBase(key, config) {
    override val distance: Double by distance
    override val beforeMovingEffects: List<Skill> by castPointEffects

    private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

    override fun cast(context: SkillContext): SkillTick<FlyDash> {
        return FlyDashTick(this, context, triggerConditionGetter.forbidden, triggerConditionGetter.interrupt)
    }
}

private class FlyDashTick(
    skill: FlyDash,
    context: SkillContext,
    override val forbiddenTriggers: Provider<TriggerConditions>,
    override val interruptTriggers: Provider<TriggerConditions>
): AbstractPlayerSkillTick<FlyDash>(skill, context) {
    override fun tickCast(tickCount: Long): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val caster = CasterUtils.getCaster<Caster.Single>(context) ?: return TickResult.INTERRUPT
        if (tickCount == 0L) {
            val casterNode = caster.toComposite()
            val newContext = SkillContext(CasterAdapter.adapt(this).toComposite(casterNode), TargetAdapter.adapt(caster))
            for (effect in skill.beforeMovingEffects) {
                FlyDashSupport.ticker.addTick(effect.cast(newContext))
            }
            return TickResult.CONTINUE_TICK
        }

        if (caster !is Caster.Single.Entity) {
            return TickResult.INTERRUPT
        }

        val entity = caster.bukkitEntity ?: return TickResult.INTERRUPT
        val direction = entity.location.direction.normalize().setY(0)
        val velocity = direction.multiply(skill.distance)
        entity.velocity = velocity

        return TickResult.ALL_DONE
    }
}

private object FlyDashSupport : KoinComponent {
    val ticker: Ticker by inject()
}