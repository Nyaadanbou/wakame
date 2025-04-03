package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability2.component.Blackhole
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.FixedPath
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
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

class BlackholeSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, Blackhole) }
), ActiveAbilitySystem {
    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCountComponent].tick
        entity.configure {
            it += AbilityTickResultComponent(tick(tickCount, entity))
        }
    }

    context(EntityUpdateContext)
    override fun tickCastPoint(tickCount: Int, entity: FleksEntity): TickResult {
        val bukkitEntity = entity[CastBy].entityOrPlayer() as? LivingEntity ?: return TickResult.RESET_STATE
        val blackhole = entity[Blackhole]

        // 设置技能选定的位置
        val rayTraceResult = bukkitEntity.rayTraceBlocks(16.0) ?: return TickResult.RESET_STATE
        val targetLocation = rayTraceResult.hitPosition.toLocation(bukkitEntity.world)
        rayTraceResult.hitBlockFace?.let { blackhole.holeDirection = it }
        blackhole.holeCenter = targetLocation

        return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickCast(tickCount: Int, entity: FleksEntity): TickResult {
        val caster = entity[CastBy].entityOrPlayer()
        val blackhole = entity[Blackhole]
        val mochaEngine = entity[AbilityComponent].mochaEngine
        val targetLocation = blackhole.holeCenter ?: return TickResult.RESET_STATE
        val radius = blackhole.radius.evaluate(mochaEngine)
        val damage = blackhole.damage.evaluate(mochaEngine)

        // 吸引周围的怪物并造成伤害
        val bukkitEntities = targetLocation.getNearbyEntities(radius, radius, radius)
        for (entity in bukkitEntities) {
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

        if (tickCount >= blackhole.duration.evaluate(mochaEngine)) {
            return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
        }

        if (tickCount % 10 == 0) {
            // 每 10 tick 生成一个粒子效果
            entity += ParticleEffectComponent(
                bukkitWorld = targetLocation.world,
                ParticleConfiguration(
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
                        axis = blackhole.holeDirection
                    ),
                    amount = 30,
                    times = 1
                ),
                ParticleConfiguration(
                    builderProvider = { loc ->
                        ParticleBuilder(Particle.FLAME)
                            .location(loc)
                            .count(1)
                            .extra(0.2)
                            .receivers(64)
                            .source(caster as? Player)
                    },
                    particlePath = FixedPath(targetLocation),
                    amount = 30,
                    times = 1
                )
            )
        }

        return TickResult.CONTINUE_TICK
    }

    context(EntityUpdateContext)
    override fun tickBackswing(tickCount: Int, entity: FleksEntity): TickResult {
        return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickReset(tickCount: Int, entity: FleksEntity): TickResult {
        val blackhole = entity[Blackhole]
        blackhole.holeDirection = BlockFace.UP
        blackhole.holeCenter = null
        entity -= ParticleEffectComponent
        return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
    }
}