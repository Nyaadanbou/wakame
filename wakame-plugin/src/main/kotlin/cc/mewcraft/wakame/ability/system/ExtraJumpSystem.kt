@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.archetype.abilitySupport
import cc.mewcraft.wakame.ability.component.ExtraJump
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.TargetTo
import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.ecs.component.WithAbility
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.eEntity
import cc.mewcraft.wakame.ecs.external.KoishEntity
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

class ExtraJumpSystem : IteratingSystem(
    family = family { all(AbilityComponent, CastBy, TargetTo, TickCountComponent, IdentifierComponent, ExtraJump) }
) {
    private lateinit var inputListener: KoishListener
    private lateinit var moveListener: KoishListener

    override fun onTickEntity(entity: Entity) {
        val extraJump = entity[ExtraJump]
        if (extraJump.cooldown > 0) {
            extraJump.cooldown--
        }
        val player = entity[CastBy].caster.entity as Player
        extraJump.isHoldingJump = player.currentInput.isJump
    }

    override fun onInit() {
        inputListener = event<PlayerInputEvent> { event ->
            val player = event.player
            val playerEEntity = player.eEntity()
            val extraJumps = playerEEntity[WithAbility].abilityEntities(AbilityArchetypes.EXTRA_JUMP)
            for (bridge in extraJumps) {
                val extraJump = bridge[ExtraJump]
                if (extraJump.jumpCount <= 0)
                    continue
                if (extraJump.cooldown > 0)
                    continue
                if (@Suppress("DEPRECATION") player.isOnGround)
                    continue
                if (extraJump.isHoldingJump)
                    continue
                if (!event.input.isJump)
                    continue
                extraJump.cooldown = ExtraJump.USE_COOLDOWN
                // 进行额外的跳跃效果, 也就是让玩家在跳跃的时候额外的向上移动一段距禽.
                val direction = player.location.direction.normalize()
                player.velocity = player.velocity.add(direction.multiply(0.3)).setY(0.6)
                // 减少跳跃次数.
                extraJump.jumpCount--

                // 播放粒子特效.
                playParticle(player, bridge)

                // 发送跳跃消息.
                extraJump.jumpedMessages.send(player)
            }
        }

        moveListener = event<PlayerMoveEvent> { event ->
            val player = event.player
            val extraJumps = player.eEntity()[WithAbility].abilityEntities(AbilityArchetypes.EXTRA_JUMP)
            for (bridge in extraJumps) {
                val extraJump = bridge[ExtraJump]
                if (extraJump.jumpCount <= 0)
                    continue
                if (event.to.y > event.from.y) {
                    extraJump.jumpCount = extraJump.originCount
                }
            }
        }
    }

    override fun onDispose() {
        inputListener.unregister()
        moveListener.unregister()
    }

    private fun playParticle(player: Player, koishEntity: KoishEntity) = abilitySupport {
        // 设置粒子特效
        koishEntity.addParticle(
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