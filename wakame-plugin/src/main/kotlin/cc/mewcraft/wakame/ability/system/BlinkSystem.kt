@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.archetype.abilitySupport
import cc.mewcraft.wakame.ability.component.Blink
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.TickResultComponent
import cc.mewcraft.wakame.ecs.data.LinePath
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.util.text.mini
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.papermc.paper.entity.TeleportFlag
import io.papermc.paper.math.Position
import org.bukkit.Particle
import org.bukkit.entity.Player

class BlinkSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, IdentifierComponent, Blink) }
), ActiveAbilitySystem {
    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCountComponent].tick
        val result = tick(deltaTime.toDouble(), tickCount, ComponentBridge(entity))
        entity.configure {
            it += TickResultComponent(result)
        }
    }

    override fun tickCastPoint(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = abilitySupport {
        val entity = componentBridge.castByEntity()

        // 如果玩家面前方块过近, 无法传送
        if (entity?.getTargetBlockExact(3) != null) {
            entity.sendMessage("<dark_red>无法传送至目标位置, 你面前的方块过近".mini)
            return@abilitySupport TickResult.RESET_STATE
        }
        return@abilitySupport TickResult.NEXT_STATE
    }

    override fun tickCast(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = abilitySupport {
        val entity = componentBridge.castByEntity() ?: return@abilitySupport TickResult.RESET_STATE
        val blink = componentBridge[Blink]
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
            blink.isTeleported = true
        }

        // 如果没有找到可传送的方块，那么就不传送
        if (!blink.isTeleported) {
            entity.sendMessage("无法传送至目标位置".mini)
            return@abilitySupport TickResult.RESET_STATE
        }

        entity.teleport(target, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z, TeleportFlag.Relative.VELOCITY_ROTATION)

        componentBridge.addParticle(
            bukkitWorld = target.world,
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

        return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickBackswing(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = abilitySupport {
        val entity = componentBridge.castByEntity() ?: return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
        val blink = componentBridge[Blink]
        if (!blink.isTeleported) {
            return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
        }

        // 再给予一个向前的固定惯性
        entity.velocity = entity.location.direction.normalize()

        blink.teleportedMessages.send(entity)
        return@abilitySupport TickResult.NEXT_STATE_NO_CONSUME
    }

    override fun tickReset(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        val blink = componentBridge[Blink]
        blink.isTeleported = false
        componentBridge -= ParticleEffectComponent
        return TickResult.NEXT_STATE_NO_CONSUME
    }
}