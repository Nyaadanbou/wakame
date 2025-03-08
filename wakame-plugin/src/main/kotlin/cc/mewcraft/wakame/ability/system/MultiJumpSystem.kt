@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.component.MultiJump
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.AbilityContainer
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.util.KoishListener
import cc.mewcraft.wakame.util.event
import com.destroystokyo.paper.ParticleBuilder
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import io.papermc.paper.math.Position
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerMoveEvent

class MultiJumpSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, IdentifierComponent, MultiJump) }
) {
    private lateinit var inputListener: KoishListener
    private lateinit var moveListener: KoishListener

    override fun onTickEntity(entity: Entity) {
        val multiJump = entity[MultiJump]
        if (multiJump.cooldown > 0) {
            multiJump.cooldown--
        }
        entity[AbilityComponent].isReadyToRemove = true
        val player = entity[CastBy].player()
        multiJump.isHoldingJump = player.currentInput.isJump
    }

    override fun onInit() {
        inputListener = event<PlayerInputEvent> { event ->
            val bukkitPlayer = event.player
            val playerEntity = bukkitPlayer.koishify()
            val multiJumps = playerEntity[AbilityContainer][AbilityArchetypes.MULTIJUMP]
            for (fleksEntity in multiJumps) {
                val multiJump = fleksEntity[MultiJump]
                if (multiJump.jumpCount <= 0)
                    continue
                if (multiJump.cooldown > 0)
                    continue
                if (bukkitPlayer.isOnGround) // 我们知道这是按照客户端发送到服务端的网络数据包为基准, 我们也是故意忽略这里造成的任何负面影响
                    continue
                if (multiJump.isHoldingJump)
                    continue
                if (!event.input.isJump)
                    continue
                multiJump.cooldown = MultiJump.USE_COOLDOWN
                // 进行额外的跳跃效果, 也就是让玩家在跳跃的时候额外的向上移动一段距禽.
                val direction = bukkitPlayer.location.direction.normalize()
                bukkitPlayer.velocity = bukkitPlayer.velocity.add(direction.multiply(0.3)).setY(0.6)
                // 减少跳跃次数.
                multiJump.jumpCount--

                // 播放粒子特效.
                playParticle(bukkitPlayer, fleksEntity)

                // 发送跳跃消息.
                multiJump.jumpedMessages.send(bukkitPlayer)
            }
        }

        moveListener = event<PlayerMoveEvent> { event ->
            val bukkitPlayer = event.player
            val koishEntities = bukkitPlayer.koishify()[AbilityContainer][AbilityArchetypes.MULTIJUMP]
            for (koishEntity in koishEntities) {
                val multiJump = koishEntity[MultiJump]
                if (multiJump.jumpCount <= 0)
                    continue
                if (event.to.y > event.from.y) {
                    multiJump.jumpCount = multiJump.originCount
                }
            }
        }
    }

    override fun onDispose() {
        inputListener.unregister()
        moveListener.unregister()
    }

    private fun playParticle(player: Player, fleksEntity: FleksEntity) {
        // 设置粒子特效
        fleksEntity.configure {
            it += ParticleEffectComponent(
                bukkitWorld = player.world,
                ParticleInfo(
                    builderProvider = { loc ->
                        ParticleBuilder(Particle.END_ROD)
                            .location(loc)
                            .receivers(64)
                            .extra(.0)
                            .source(player)
                    },
                    particlePath = CirclePath(
                        center = Position.fine(player.location.x, player.location.y, player.location.z),
                        radius = 0.5,
                        axis = BlockFace.UP
                    ),
                    times = 1
                )
            )
        }
    }
}