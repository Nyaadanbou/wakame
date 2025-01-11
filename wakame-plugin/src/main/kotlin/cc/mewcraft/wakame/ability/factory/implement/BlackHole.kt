package cc.mewcraft.wakame.ability.factory.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.ActiveAbilityMechanic
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.factory.AbilityFactory
import cc.mewcraft.wakame.ability.factory.abilitySupport
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.util.krequire
import com.destroystokyo.paper.ParticleBuilder
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode

/**
 * 选定一个位置为中心, 将该位置周围的怪物都吸引到该位置, 并造成伤害.
 */
interface BlackHole : Ability {

    val radius: Evaluable<*>

    val duration: Evaluable<*>

    val damage: Evaluable<*>

    companion object : AbilityFactory<BlackHole> {
        override fun create(key: Key, config: ConfigurationNode): BlackHole {
            val radius = config.node("radius").krequire<Evaluable<*>>()
            val duration = config.node("duration").krequire<Evaluable<*>>()
            val damage = config.node("damage").krequire<Evaluable<*>>()
            return Impl(key, config, radius, duration, damage)
        }
    }

    private class Impl(
        key: Key,
        config: ConfigurationNode,
        override val radius: Evaluable<*>,
        override val duration: Evaluable<*>,
        override val damage: Evaluable<*>,
    ) : BlackHole, AbilityBase(key, config) {
        override fun mechanic(input: AbilityInput): Mechanic {
            return BlackHoleAbilityMechanic(radius, duration, damage)
        }
    }
}

private class BlackHoleAbilityMechanic(
    private val radius: Evaluable<*>,
    private val duration: Evaluable<*>,
    private val damage: Evaluable<*>,
) : ActiveAbilityMechanic() {
    private var blockFace: BlockFace = BlockFace.UP

    override fun tickCastPoint(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        val entity = componentMap.castByEntity()

        // 设置技能选定的位置
        val rayTranceResult = entity.rayTraceBlocks(16.0) ?: return TickResult.RESET_STATE
        val targetLocation = rayTranceResult.hitPosition.toLocation(entity.world)
        rayTranceResult.hitBlockFace?.let { blockFace = it }
        componentMap += TargetTo(targetLocation.toTarget())

        return TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickCast(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        val caster = componentMap.castByEntity()
        val targetLocation = componentMap.targetToLocation()
        val radius = componentMap.evaluate(radius)
        val damage = componentMap.evaluate(damage)

        // 吸引周围的怪物并造成伤害
        val entities = targetLocation.getNearbyEntities(radius, radius, radius)
        for (entity in entities) {
            if (entity == caster) {
                continue
            }

            if (entity is LivingEntity) {
                entity.teleport(targetLocation)
                entity.damage(damage)
            }
        }

        if (tickCount >= componentMap.evaluate(duration)) {
            return TickResult.NEXT_STATE_NO_CONSUME
        }

        if (tickCount % 10 == 0.0) {
            // 每 10 tick 生成一个粒子效果
            componentMap += ParticleEffectComponent(
                builderProvider = { loc ->
                    ParticleBuilder(Particle.DUST_COLOR_TRANSITION)
                        .location(loc)
                        .count(1)
                        .extra(0.0)
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
            )
        }

        return TickResult.CONTINUE_TICK
    }

    override fun tickBackswing(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        return TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickReset(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        blockFace = BlockFace.UP
        componentMap -= ParticleEffectComponent
        return TickResult.NEXT_STATE_NO_CONSUME
    }
}