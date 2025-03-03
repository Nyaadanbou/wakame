@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.PassiveAbilityMechanic
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilitySupport.castByEntity
import cc.mewcraft.wakame.ability.archetype.abilitySupport
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.data.CirclePath
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.util.event.Events
import cc.mewcraft.wakame.util.event.Subscription
import cc.mewcraft.wakame.util.require
import com.destroystokyo.paper.ParticleBuilder
import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

object ExtraJumpArchetype : AbilityArchetype {
    override fun create(key: Key, config: ConfigurationNode): Ability {
        val count = config.node("count").require<Int>()
        val jumpedMessages = config.node("jumped_messages").get<AudienceMessageGroup>() ?: AudienceMessageGroup.empty()
        return ExtraJump(key, config, count, jumpedMessages)
    }
}

private class ExtraJump(
    key: Key,
    config: ConfigurationNode,
    val count: Int,
    val jumpedMessages: AudienceMessageGroup,
) : Ability(key, config) {
    override fun mechanic(input: AbilityInput): Mechanic {
        return ExtraJumpAbilityMechanic(this)
    }
}

private class ExtraJumpAbilityMechanic(
    private val extraJump: ExtraJump,
) : PassiveAbilityMechanic() {
    companion object {
        private const val USE_COOLDOWN = 5L
    }

    private lateinit var jumpSubscription: Subscription
    private lateinit var touchGroundSubscription: Subscription

    private var cooldown: Long = USE_COOLDOWN
    private var isHoldingJump: Boolean = false
    private var jumpCount: Int = extraJump.count

    override fun passiveTick(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge) {
        if (cooldown > 0) {
            cooldown--
        }
        val player = componentBridge.castByEntity() as Player

        if (player.currentInput.isJump) {
            isHoldingJump = true
        } else {
            isHoldingJump = false
        }
    }

    override fun onEnable(componentBridge: ComponentBridge) = abilitySupport {
        jumpSubscription = Events.subscribe(PlayerInputEvent::class.java)
            .filter { it.player == componentBridge.castByEntity() }
            .filter { jumpCount > 0 }
            .filter { cooldown <= 0 }
            .filter { @Suppress("DEPRECATION") !it.player.isOnGround } // Spigot 弃用此方法的原因是客户端可以通过发包来欺骗服务端, 我们这里不考虑挂端欺骗的情况.
            .filter { !isHoldingJump } // 如果玩家在跳跃时按住跳跃键的时间超过 1t, 则不进行额外的跳跃效果. 防止玩家一直按住跳跃键而误触发跳跃效果.
            .filter { it.input.isJump.also { if (it) cooldown = USE_COOLDOWN } }
            .handler {
                // 进行额外的跳跃效果, 也就是让玩家在跳跃的时候额外的向上移动一段距禽.
                val player = it.player
                val direction = player.location.direction.normalize()
                player.velocity = player.velocity.add(direction.multiply(0.3)).setY(0.6)
                // 减少跳跃次数.
                jumpCount--

                // 播放粒子特效.
                playParticle(player, componentBridge)

                // 发送跳跃消息.
                extraJump.jumpedMessages.send(player)
            }

        touchGroundSubscription = Events.subscribe(PlayerMoveEvent::class.java)
            .filter { it.player == componentBridge.castByEntity() }
            .filter { @Suppress("DEPRECATION") it.player.isOnGround }
            .handler {
                // 重置跳跃状态.
                jumpCount = extraJump.count
            }
    }

    override fun onDisable(componentBridge: ComponentBridge) {
        jumpSubscription.unregister()
        touchGroundSubscription.unregister()
    }

    private fun playParticle(player: Player, componentBridge: ComponentBridge) = abilitySupport {
        // 设置粒子特效
        componentBridge.addParticle(
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