package cc.mewcraft.wakame.ability.archetype

import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ability.character.Target
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ecs.component.*
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.molang.Evaluable
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.LivingEntity

@DslMarker
internal annotation class AbilitySupportDsl

internal inline fun <T> abilitySupport(run: AbilitySupport.() -> T): T = AbilitySupport.run()

/**
 * 存放一些技能相关的支持函数.
 */
@AbilitySupportDsl
internal object AbilitySupport {
    fun ComponentBridge.castBy(): Caster? {
        return this[CastBy]?.caster
    }

    fun ComponentBridge.castByEntity(): LivingEntity? {
        return this[CastBy]?.caster?.entity as? LivingEntity
    }

    fun ComponentBridge.targetTo(): Target {
        return this[TargetTo]?.target ?: error("No entity found in TargetTo component")
    }

    fun ComponentBridge.evaluate(evaluable: Evaluable<*>): Double {
        val engine = this[AbilityComponent]?.mochaEngine
        return if (engine != null) {
            evaluable.evaluate(engine)
        } else {
            evaluable.evaluate()
        }
    }

    fun ComponentBridge.addParticle(bukkitWorld: World, vararg particleInfos: ParticleInfo) {
        val component = this[ParticleEffectComponent]
        if (component != null) {
            component.particleInfos.addAll(particleInfos)
        } else {
            this += ParticleEffectComponent(bukkitWorld, *particleInfos)
        }
    }

    private var ComponentBridge.tickCount: Double
        get() = this[TickCountComponent]?.tick ?: error("No TickCountComponent found")
        set(value) {
            this[TickCountComponent]?.tick = value
        }

    fun Location.toTarget(): Target {
        return TargetAdapter.adapt(this)
    }
}