package cc.mewcraft.wakame.ability.factory

import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.external.ComponentMap
import org.bukkit.entity.LivingEntity

@DslMarker
internal annotation class AbilitySupportDsl

internal fun <T> abilitySupport(dsl: AbilitySupport.() -> T): T = AbilitySupport.dsl()

/**
 * 存放一些技能相关的支持函数.
 */
@AbilitySupportDsl
internal object AbilitySupport {
    fun ComponentMap.castByEntity(): LivingEntity {
        return this[CastBy]?.entity as? LivingEntity ?: error("No entity found in CastBy component")
    }

    var ComponentMap.tickCount: Double
        get() = this[TickCountComponent]?.tick ?: error("No TickCountComponent found")
        set(value) {
            this[TickCountComponent]?.tick = value
        }
}