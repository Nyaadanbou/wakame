@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.component.AbilityContainer
import cc.mewcraft.wakame.ability2.component.CastBy
import cc.mewcraft.wakame.ability2.component.MultiJump
import cc.mewcraft.wakame.ability2.component.TargetTo
import cc.mewcraft.wakame.ability2.meta.AbilityMetaTypes
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.ParticleConfiguration
import cc.mewcraft.wakame.ecs.system.ListenableIteratingSystem
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World.Companion.family
import io.papermc.paper.math.Position
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerMoveEvent

class MultiJumpSystem : ListenableIteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, MultiJump) }
) {
    override fun onTickEntity(entity: Entity) {
        entity[AbilityComponent].isReadyToRemove = true
        val multijump = entity[MultiJump]
        if (multijump.cooldown > 0) {
            multijump.cooldown--
        }
        val player = entity[CastBy].player()
        multijump.isHoldingJump = player.currentInput.isJump
    }

    @EventHandler
    private fun onPlayerInput(event: PlayerInputEvent) {
        val player = event.player
        val playerEntity = player.koishify()
        val multijumpEntities = playerEntity[AbilityContainer][AbilityMetaTypes.MULTI_JUMP]
        for (multijumpEntity in multijumpEntities) {
            val multijump = multijumpEntity[MultiJump]
            if (multijump.jumpCount <= 0)
                continue
            if (multijump.cooldown > 0)
                continue
            if (player.isOnGround) // 我们知道这是按照客户端发送到服务端的网络数据包为基准, 我们也是故意忽略这里造成的任何负面影响
                continue
            if (multijump.isHoldingJump)
                continue
            if (!event.input.isJump)
                continue

            multijump.cooldown = MultiJump.USE_COOLDOWN

            // 进行额外的跳跃效果, 也就是让玩家在跳跃的时候额外的向上移动一段距禽.
            val direction = player.location.direction.normalize()
            player.velocity = player.velocity.add(direction.multiply(0.3)).setY(0.6)

            // 减少跳跃次数.
            multijump.jumpCount--

            // 播放粒子特效.
            playParticle(player, multijumpEntity)
            // 发送跳跃消息.
            multijump.jumpedMessages.send(player)
        }
    }

    @EventHandler
    private fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if (!player.isOnGround)
            return
        val multijumpEntities = player.koishify()[AbilityContainer][AbilityMetaTypes.MULTI_JUMP]
        for (multijumpEntity in multijumpEntities) {
            val multiJump = multijumpEntity[MultiJump]
            if (multiJump.jumpCount > 0)
                continue
            multiJump.jumpCount = multiJump.originCount
        }
    }

    private fun playParticle(player: Player, entity: EEntity) {
        // 设置粒子特效
        entity.configure {
            it += ParticleEffectComponent(
                world = player.world,
                ParticleConfiguration(
                    builderProvider = { loc ->
                        ParticleBuilder(Particle.END_ROD)
                            .location(loc)
                            .receivers(32)
                            .extra(.0)
                            .source(player)
                    },
                    particlePath = CirclePath(
                        center = Position.fine(player.location.x, player.location.y, player.location.z),
                        radius = 0.5,
                        axis = BlockFace.UP
                    ),
                    amount = 8,
                    times = 1
                )
            )
        }
    }
}