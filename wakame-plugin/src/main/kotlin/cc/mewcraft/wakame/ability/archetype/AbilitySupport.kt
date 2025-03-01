package cc.mewcraft.wakame.ability.archetype

import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ability.character.Target
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.molang.Evaluable
import org.bukkit.Location
import org.bukkit.entity.LivingEntity

@DslMarker
internal annotation class AbilitySupportDsl

internal inline fun <T> abilitySupport(run: AbilitySupport.() -> T): T = AbilitySupport.run()

/**
 * 存放一些技能相关的支持函数.
 */
@AbilitySupportDsl
internal object AbilitySupport {
    fun ComponentMap.castBy(): Caster? {
        return this[CastBy]?.caster
    }

    fun ComponentMap.castByEntity(): LivingEntity? {
        return this[CastBy]?.caster?.entity as? LivingEntity
    }

    fun ComponentMap.targetTo(): Target {
        return this[TargetTo]?.target ?: error("No entity found in TargetTo component")
    }

    fun ComponentMap.evaluate(evaluable: Evaluable<*>): Double {
        val engine = this[AbilityComponent]?.mochaEngine
        return if (engine != null) {
            evaluable.evaluate(engine)
        } else {
            evaluable.evaluate()
        }
    }

    fun ComponentMap.addParticle(vararg particleInfos: ParticleInfo) {
        val component = this[ParticleEffectComponent]
        if (component != null) {
            component.particleInfos.addAll(particleInfos)
        } else {
            this += ParticleEffectComponent(*particleInfos)
        }
    }

    private var ComponentMap.tickCount: Double
        get() = this[TickCountComponent]?.tick ?: error("No TickCountComponent found")
        set(value) {
            this[TickCountComponent]?.tick = value
        }

    fun Location.toTarget(): Target {
        return TargetAdapter.adapt(this)
    }
}