package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.ActiveAbilityMechanic
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.abilitySupport
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.FixedPath
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.util.krequire
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode

/**
 * 选定一个位置为中心, 将该位置周围的怪物都吸引到该位置, 并造成伤害.
 */
object BlackHoleArchetype : AbilityArchetype {
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val radius = config.node("radius").krequire<Evaluable<*>>()
        val duration = config.node("duration").krequire<Evaluable<*>>()
        val damage = config.node("damage").krequire<Evaluable<*>>()
        return BlackHole(key, config, radius, duration, damage)
    }
}

private class BlackHole(
    key: Key,
    config: ConfigurationNode,
    val radius: Evaluable<*>,
    val duration: Evaluable<*>,
    val damage: Evaluable<*>,
) : Ability(key, config) {
    override fun mechanic(input: AbilityInput): Mechanic {
        return BlackHoleAbilityMechanic(this)
    }
}

private class BlackHoleAbilityMechanic(
    private val blackHole: BlackHole,
) : ActiveAbilityMechanic() {
    private var holeLocation: Location? = null
    private var blockFace: BlockFace = BlockFace.UP

    override fun tickCastPoint(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        val entity = componentMap.castByEntity()

        // 设置技能选定的位置
        if (entity != null) {
            val rayTranceResult = entity.rayTraceBlocks(16.0) ?: return TickResult.RESET_STATE
            val targetLocation = rayTranceResult.hitPosition.toLocation(entity.world)
            rayTranceResult.hitBlockFace?.let { blockFace = it }
            holeLocation = targetLocation
        }

        return TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickCast(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        val caster = componentMap.castByEntity()
        val targetLocation = holeLocation ?: return TickResult.RESET_STATE
        val radius = componentMap.evaluate(blackHole.radius)
        val damage = componentMap.evaluate(blackHole.damage)

        // 吸引周围的怪物并造成伤害
        val entities = targetLocation.getNearbyEntities(radius, radius, radius)
        for (entity in entities) {
            if (entity == caster) {
                continue
            }

            if (entity is LivingEntity) {
                // 给生物给予一个向目标位置的速度
                val direction = targetLocation.toVector().subtract(entity.location.toVector()).normalize()
                entity.velocity = direction.multiply(1.0)
                entity.damage(damage)
            }
        }

        if (tickCount >= componentMap.evaluate(blackHole.duration)) {
            return TickResult.NEXT_STATE_NO_CONSUME
        }

        if (tickCount % 10 == 0.0) {
            // 每 10 tick 生成一个粒子效果
            componentMap.addParticle(
                ParticleInfo(
                    builderProvider = { loc ->
                        ParticleBuilder(Particle.DUST_COLOR_TRANSITION)
                            .location(loc)
                            .count(1)
                            .extra(0.2)
                            .colorTransition(Color.BLACK, Color.ORANGE)
                            .receivers(64)
                            .source(caster as? Player)
                    },
                    particlePath = CirclePath(
                        center = targetLocation,
                        radius = radius,
                        axis = blockFace
                    ),
                    times = 1
                ),
                ParticleInfo(
                    builderProvider = { loc ->
                        ParticleBuilder(Particle.FLAME)
                            .location(loc)
                            .count(1)
                            .extra(0.2)
                            .receivers(64)
                            .source(caster as? Player)
                    },
                    particlePath = FixedPath(targetLocation),
                    times = 1
                )
            )
        }

        return TickResult.CONTINUE_TICK
    }

    override fun tickBackswing(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        return TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickReset(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        blockFace = BlockFace.UP
        holeLocation = null
        componentMap -= ParticleEffectComponent
        return TickResult.NEXT_STATE_NO_CONSUME
    }
}