@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.StatePhase
import cc.mewcraft.wakame.ability.component.*
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.bridge.EWorld
import cc.mewcraft.wakame.ecs.component.ParticleEffect
import cc.mewcraft.wakame.ecs.component.TickCount
import cc.mewcraft.wakame.ecs.data.LinePath
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import cc.mewcraft.wakame.ecs.plusAssign
import cc.mewcraft.wakame.util.text.mini
import com.destroystokyo.paper.MaterialSetTag
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.EntityUpdateContext
import com.github.quillraven.fleks.IteratingSystem
import io.papermc.paper.entity.TeleportFlag
import io.papermc.paper.math.Position
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

object TickAbilityBlink : IteratingSystem(
    family = EWorld.family { all(Ability, CastBy, TargetTo, TickCount, Blink) }
), AbilitySkeleton {
    override fun onTickEntity(entity: Entity) {
        val tickCount = entity[TickCount].tick
        entity.configure {
            entity[Ability].phase = tick(tickCount, entity[Ability].phase, entity)
        }
    }

    context(_: EntityUpdateContext)
    override fun tickCastPoint(tickCount: Int, entity: EEntity): StatePhase {
        val bukkitEntity = entity[CastBy].entityOrPlayer() as? LivingEntity

        // 如果玩家面前方块过近, 无法传送
        if (bukkitEntity?.getTargetBlockExact(3) != null) {
            bukkitEntity.sendMessage("<dark_red>无法传送至目标位置, 你面前的方块过近".mini)
            return StatePhase.Reset()
        }
        return StatePhase.Casting(true)
    }

    context(_: EntityUpdateContext)
    override fun tickCast(tickCount: Int, entity: EEntity): StatePhase {
        val bukkitEntity = entity[CastBy].entityOrPlayer() as? LivingEntity ?: return StatePhase.Reset()
        val blink = entity[Blink]
        val location = bukkitEntity.location.clone()

        // 计算目标位置
        val target = location.clone()

        // 获取视线上的所有方块, 并将其从远到近排序
        val blocks = bukkitEntity.getLineOfSight(MaterialSetTag.REPLACEABLE.values, blink.distance)

        // 如果没有找到可传送的方块，那么就不传送
        if (blocks.isEmpty()) {
            return StatePhase.Reset()
        }

        // 遍历所有方块，试图找到第一个可传送的方块
        for (block in blocks) {
            // 如果方块没有 1x2x1 的空间，那么就不能传送
            if (!block.isEmpty) continue
            if (!block.getRelative(0, 1, 0).isEmpty) continue

            // 所有条件都满足，可以传送
            target.x = block.x + 0.1
            target.y = block.y + 0.1
            target.z = block.z + 0.1
            blink.isTeleported = true
        }

        bukkitEntity.teleport(target, TeleportFlag.Relative.VELOCITY_X, TeleportFlag.Relative.VELOCITY_Y, TeleportFlag.Relative.VELOCITY_Z, TeleportFlag.Relative.VELOCITY_ROTATION)
        bukkitEntity.fallDistance = 0f
        // 再给予一个向前的固定惯性
        bukkitEntity.velocity = bukkitEntity.location.direction.normalize()

        blink.teleportedMessages.send(bukkitEntity)

        entity += ParticleEffect(
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
                ),
                times = 1
            )
        )

        return StatePhase.Backswing()
    }

    context(_: EntityUpdateContext)
    override fun tickReset(tickCount: Int, entity: EEntity): StatePhase {
        val blink = entity[Blink]
        if (!blink.isTeleported) {
            // TODO: 发送取消传送消息
            return StatePhase.Idle()
        }
        blink.isTeleported = false
        return StatePhase.Idle()
    }
}