package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.skill.*
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.tick.AbstractPlayerSkillTick
import cc.mewcraft.wakame.skill.tick.SkillTick
import cc.mewcraft.wakame.tick.TickResult
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

interface FlyDash : Skill {
    val distance: Double

    val beforeMovingEffects: List<SkillProvider>

    companion object Factory : SkillFactory<FlyDash> {
        override fun create(key: Key, config: ConfigurationNode): FlyDash {
            val distance = config.node("distance").krequire<Double>()
            val castPointEffects = config.node("before_moving_effects").get<List<SkillProvider>>() ?: emptyList()
            return FlyDashDefaultImpl(key, config, distance, castPointEffects)
        }
    }
}

private class FlyDashDefaultImpl(
    key: Key,
    config: ConfigurationNode,
    override val distance: Double,
    override val beforeMovingEffects: List<SkillProvider>,
): FlyDash, SkillBase(key, config) {
    private val triggerConditionGetter: TriggerConditionGetter = TriggerConditionGetter()

    override fun cast(context: SkillContext): SkillTick<FlyDash> {
        return FlyDashTick(this, context, triggerConditionGetter.forbidden, triggerConditionGetter.interrupt)
    }
}

private class FlyDashTick(
    skill: FlyDash,
    context: SkillContext,
    override val forbiddenTriggers: TriggerConditions,
    override val interruptTriggers: TriggerConditions
): AbstractPlayerSkillTick<FlyDash>(skill, context) {
    override fun tickCast(tickCount: Long): TickResult {
        if (!checkConditions())
            return TickResult.ALL_DONE
        val caster = CasterUtils.getCaster<Caster.Single>(context) ?: return TickResult.INTERRUPT
        if (tickCount == 0L) {
            val casterNode = caster.toComposite()
            val newContext = SkillContext(CasterAdapter.adapt(this).toComposite(casterNode), TargetAdapter.adapt(caster))
            for (effect in skill.beforeMovingEffects) {
                FlyDashSupport.ticker.addTick(effect.get().cast(newContext))
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