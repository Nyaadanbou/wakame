@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.ActiveAbilityMechanic
import cc.mewcraft.wakame.ability.character.TargetAdapter
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.abilitySupport
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.data.LinePath
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.text.mini
import com.destroystokyo.paper.ParticleBuilder
import io.papermc.paper.entity.TeleportFlag
import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

/**
 * 短距离瞬移技能.
 */
object BlinkArchetype : AbilityArchetype {
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val distance = config.node("distance").require<Int>()
        val teleportedMessages = config.node("teleported_messages").get<AudienceMessageGroup>() ?: AudienceMessageGroup.empty()

        return Blink(key, config, distance, teleportedMessages)
    }
}

private class Blink(
    key: Key,
    config: ConfigurationNode,
    val distance: Int,
    val teleportedMessages: AudienceMessageGroup,
) : AbilityBase(key, config) {
    override fun mechanic(input: AbilityInput): Mechanic {
        return BlinkAbilityMechanic(this)
    }
}

private class BlinkAbilityMechanic(
    private val blink: Blink
) : ActiveAbilityMechanic() {

    private var isTeleported: Boolean = false

    override fun tickCastPoint(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        val entity = componentMap.castByEntity()

        // 如果玩家面前方块过近, 无法传送
        if (entity.getTargetBlockExact(3) != null) {
            entity.sendMessage("<dark_red>无法传送至目标位置, 你面前的方块过近".mini)
            return@abilitySupport TickResult.RESET_STATE
        }
        return@abilitySupport TickResult.NEXT_STATE
    }

    override fun tickCast(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        val entity = componentMap.castByEntity()
        val location = entity.location.clone()

        // 计算目标位置
        val target = location.clone()

        // 获取视线上的所有方块, 并将其从远到近排序
        val blocks = entity.getLineOfSight(null, blink.distance)

        // 遍历所有方块，试图找到第一个可传送的方块
        for (block in blocks) {
            // 如果方块没有 1x2x1 的空间，那么就不能传送
            if (!block.isEmpty) continue
            if (!block.getRelative(0, 1, 0).isEmpty) continue

            // 所有条件都满足，可以传送
            target.x = block.x + 0.5
            target.y = block.y.toDouble()
            target.z = block.z + 0.5
            isTeleported = true
        }

        // 如果没有找到可传送的方块，那么就不传送
        if (!isTeleported) {
            entity.sendMessage("无法传送至目标位置".mini)
            return@abilitySupport TickResult.RESET_STATE
        }

        entity.teleport(target, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z, TeleportFlag.Relative.VELOCITY_ROTATION)

        componentMap.addParticle(
            ParticleInfo(
                builderProvider = { loc ->
                    ParticleBuilder(Particle.END_ROD)
                        .location(loc)
                        .receivers(64)
                        .extra(.0)
                        .source(entity as? Player)
                },
                particlePath = LinePath(
                    start = Position.fine(location),
                    end = Position.fine(target)
                )
            )
        )
        componentMap += TargetTo(TargetAdapter.adapt(target))

        return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickBackswing(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = abilitySupport {
        val entity = componentMap.castByEntity()
        if (!isTeleported) {
            return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
        }

        // 再给予一个向前的固定惯性
        entity.velocity = entity.location.direction.normalize()

        blink.teleportedMessages.send(entity)
        return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickReset(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        isTeleported = false
        componentMap -= ParticleEffectComponent
        componentMap -= TargetTo
        return TickResult.NEXT_STATE_NO_CONSUME
    }
}