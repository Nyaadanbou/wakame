@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityTickResultComponent
import cc.mewcraft.wakame.ability2.component.Blink
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.data.LinePath
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import cc.mewcraft.wakame.util.text.mini
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.papermc.paper.entity.TeleportFlag
import io.papermc.paper.math.Position
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object BlinkSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, Blink) }
), ActiveAbilitySystem {
    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCountComponent].tick
        entity.configure {
            it += AbilityTickResultComponent(tick(tickCount, entity))
        }
    }

    context(EntityUpdateContext)
    override fun tickCastPoint(tickCount: Int, entity: EEntity): TickResult {
        val bukkitEntity = entity[CastBy].entityOrPlayer() as? LivingEntity

        // 如果玩家面前方块过近, 无法传送
        if (bukkitEntity?.getTargetBlockExact(3) != null) {
            bukkitEntity.sendMessage("<dark_red>无法传送至目标位置, 你面前的方块过近".mini)
            return TickResult.RESET_STATE
        }
        return TickResult.ADVANCE_TO_NEXT_STATE
    }

    context(EntityUpdateContext)
    override fun tickCast(tickCount: Int, entity: EEntity): TickResult {
        val bukkitEntity = entity[CastBy].entityOrPlayer() as? LivingEntity ?: return TickResult.RESET_STATE
        val blink = entity[Blink]
        val location = bukkitEntity.location.clone()

        // 计算目标位置
        val target = location.clone()

        // 获取视线上的所有方块, 并将其从远到近排序
        val blocks = bukkitEntity.getLineOfSight(null, blink.distance)

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
            bukkitEntity.sendMessage("无法传送至目标位置".mini)
            return TickResult.RESET_STATE
        }

        bukkitEntity.teleport(target, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z, TeleportFlag.Relative.VELOCITY_ROTATION)
        bukkitEntity.fallDistance = 0f

        entity += ParticleEffectComponent(
            world = target.world,
            ParticleConfiguration(
                builderProvider = { loc ->
                    ParticleBuilder(Particle.END_ROD)
                        .location(loc)
                        .receivers(64)
                        .extra(.0)
                        .source(bukkitEntity as? Player)
                },
                amount = 10,
                particlePath = LinePath(
                    start = Position.fine(location),
                    end = Position.fine(target)
                )
            )
        )

        return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickBackswing(tickCount: Int, entity: EEntity): TickResult {
        val bukkitEntity = entity[CastBy].entityOrPlayer() as? LivingEntity ?: return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
        val blink = entity[Blink]
        if (!blink.isTeleported) {
            return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
        }

        // 再给予一个向前的固定惯性
        bukkitEntity.velocity = bukkitEntity.location.direction.normalize()

        blink.teleportedMessages.send(bukkitEntity)
        return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
    }

    context(EntityUpdateContext)
    override fun tickReset(tickCount: Int, entity: EEntity): TickResult {
        val blink = entity[Blink]
        blink.isTeleported = false
        entity -= ParticleEffectComponent
        return TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME
    }
}