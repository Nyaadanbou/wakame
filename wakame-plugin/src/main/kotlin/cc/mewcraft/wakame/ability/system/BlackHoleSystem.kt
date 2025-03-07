package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.BlackHole
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.FixedPath
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.data.TickResult
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class BlackHoleSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, IdentifierComponent, BlackHole) }
), ActiveAbilitySystem {
    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCountComponent].tick
        entity.configure {
            it += TickResultComponent(tick(deltaTime.toDouble(), tickCount, entity))
        }
    }

    context(EntityUpdateContext)
    override fun tickCastPoint(deltaTime: Double, tickCount: Double, fleksEntity: FleksEntity): TickResult {
        val entity = fleksEntity[CastBy].entityOrPlayer() as? LivingEntity ?: return TickResult.RESET_STATE
        val blackHole = fleksEntity[BlackHole]

        // 设置技能选定的位置
        val rayTranceResult = entity.rayTraceBlocks(16.0) ?: return TickResult.RESET_STATE
        val targetLocation = rayTranceResult.hitPosition.toLocation(entity.world)
        rayTranceResult.hitBlockFace?.let { blackHole.blockFace = it }
        blackHole.holeLocation = targetLocation

        return TickResult.NEXT_STATE_NO_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickCast(deltaTime: Double, tickCount: Double, fleksEntity: FleksEntity): TickResult {
        val caster = fleksEntity[CastBy].entityOrPlayer()
        val blackHole = fleksEntity[BlackHole]
        val mochaEngine = fleksEntity[AbilityComponent].mochaEngine
        val targetLocation = blackHole.holeLocation ?: return TickResult.RESET_STATE
        val radius = blackHole.radius.evaluate(mochaEngine)
        val damage = blackHole.damage.evaluate(mochaEngine)

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

        if (tickCount >= blackHole.duration.evaluate(mochaEngine)) {
            return TickResult.NEXT_STATE_NO_CONSUME
        }

        if (tickCount % 10 == 0.0) {
            // 每 10 tick 生成一个粒子效果
            fleksEntity += ParticleEffectComponent(
                bukkitWorld = targetLocation.world,
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
                        axis = blackHole.blockFace
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

    context(EntityUpdateContext)
    override fun tickBackswing(deltaTime: Double, tickCount: Double, fleksEntity: FleksEntity): TickResult {
        return TickResult.NEXT_STATE_NO_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickReset(deltaTime: Double, tickCount: Double, fleksEntity: FleksEntity): TickResult {
        val blackHole = fleksEntity[BlackHole]
        blackHole.blockFace = BlockFace.UP
        blackHole.holeLocation = null
        fleksEntity -= ParticleEffectComponent
        return TickResult.NEXT_STATE_NO_CONSUME
    }
}