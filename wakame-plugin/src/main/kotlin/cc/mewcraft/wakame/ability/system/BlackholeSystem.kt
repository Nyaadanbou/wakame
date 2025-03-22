package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.component.AbilityArchetypeComponent
import cc.mewcraft.wakame.ability.component.AbilityComponent
import cc.mewcraft.wakame.ability.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability.component.Blackhole
import cc.mewcraft.wakame.ability.component.CastBy
import cc.mewcraft.wakame.ability.component.TargetTo
import cc.mewcraft.wakame.ability.data.TickResult
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
    family = family { all(AbilityComponent, CastBy, TargetTo, AbilityArchetypeComponent, Blackhole) }
), ActiveAbilitySystem {
    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCountComponent].tick
        entity.configure {
            it += AbilityTickResultComponent(tick(tickCount, entity))
        }
    }

    context(EntityUpdateContext)
    override fun tickCastPoint(tickCount: Int, fleksEntity: FleksEntity): TickResult {
        val entity = fleksEntity[CastBy].entityOrPlayer() as? LivingEntity ?: return TickResult.RESET_STATE
        val blackhole = fleksEntity[Blackhole]

        // 设置技能选定的位置
        val rayTraceResult = entity.rayTraceBlocks(16.0) ?: return TickResult.RESET_STATE
        val targetLocation = rayTraceResult.hitPosition.toLocation(entity.world)
        rayTraceResult.hitBlockFace?.let { blackhole.holeDirection = it }
        blackhole.holeCenter = targetLocation

        return TickResult.ADVANCE_NEXT_STATE_WITHOUT_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickCast(tickCount: Int, fleksEntity: FleksEntity): TickResult {
        val caster = fleksEntity[CastBy].entityOrPlayer()
        val blackhole = fleksEntity[Blackhole]
        val mochaEngine = fleksEntity[AbilityComponent].mochaEngine
        val targetLocation = blackhole.holeCenter ?: return TickResult.RESET_STATE
        val radius = blackhole.radius.evaluate(mochaEngine)
        val damage = blackhole.damage.evaluate(mochaEngine)

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

        if (tickCount >= blackhole.duration.evaluate(mochaEngine)) {
            return TickResult.ADVANCE_NEXT_STATE_WITHOUT_CONSUME
        }

        if (tickCount % 10 == 0) {
            // 每 10 tick 生成一个粒子效果
            fleksEntity += ParticleEffectComponent(
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
    override fun tickBackswing(tickCount: Int, fleksEntity: FleksEntity): TickResult {
        return TickResult.ADVANCE_NEXT_STATE_WITHOUT_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickReset(tickCount: Int, fleksEntity: FleksEntity): TickResult {
        val blackhole = fleksEntity[Blackhole]
        blackhole.holeDirection = BlockFace.UP
        blackhole.holeCenter = null
        fleksEntity -= ParticleEffectComponent
        return TickResult.ADVANCE_NEXT_STATE_WITHOUT_CONSUME
    }
}