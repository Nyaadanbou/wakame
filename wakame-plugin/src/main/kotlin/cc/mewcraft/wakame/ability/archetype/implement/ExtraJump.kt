@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.archetype.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.PassiveAbilityMechanic
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.abilitySupport
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.data.ParticleInfo
import cc.mewcraft.wakame.ecs.data.SpiralPath
import cc.mewcraft.wakame.ecs.external.ComponentBridge
import cc.mewcraft.wakame.util.event.Events
import cc.mewcraft.wakame.util.event.Subscription
import cc.mewcraft.wakame.util.require
import com.destroystokyo.paper.ParticleBuilder
import io.papermc.paper.math.Position
import net.kyori.adventure.key.Key
import org.bukkit.Particle
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
    private var jumpCount: Int = extraJump.count

    override fun passiveTick(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge) {
        if (cooldown > 0) {
            cooldown--
        }
    }

    override fun onEnable(componentBridge: ComponentBridge) = abilitySupport {
        jumpSubscription = Events.subscribe(PlayerInputEvent::class.java)
            .filter { it.player == componentBridge.castByEntity() }
            .filter { jumpCount > 0 }
            .filter { cooldown <= 0 }
            .filter { it.input.isJump.also { if (it) cooldown = USE_COOLDOWN } }
            .filter { @Suppress("DEPRECATION") !it.player.isOnGround } // Spigot 弃用此方法的原因是客户端可以通过发包来欺骗服务端, 我们这里不考虑挂端欺骗的情况.
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
                particlePath = SpiralPath(
                    center = Position.fine(player.location.x, player.location.y, player.location.z),
                    radius = 0.5,
                    height = 0.5,
                    rotations = 1
                ),
                times = 1
            )
        )
    }

}