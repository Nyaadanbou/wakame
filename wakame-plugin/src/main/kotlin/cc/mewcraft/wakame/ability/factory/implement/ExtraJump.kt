@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.factory.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.AbilityMechanic
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.factory.AbilityFactory
import cc.mewcraft.wakame.ability.factory.abilitySupport
import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.ecs.component.ParticleEffectComponent
import cc.mewcraft.wakame.ecs.data.SpiralPath
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.util.krequire
import com.destroystokyo.paper.ParticleBuilder
import io.papermc.paper.math.Position
import me.lucko.helper.Events
import me.lucko.helper.event.Subscription
import net.kyori.adventure.key.Key
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get

interface ExtraJump : Ability {
    val count: Int

    val jumpedMessages: AudienceMessageGroup

    companion object Factory : AbilityFactory<ExtraJump> {
        override fun create(key: Key, config: ConfigurationNode): ExtraJump {
            val count = config.node("count").krequire<Int>()
            val jumpedMessages = config.node("jumped_messages").get<AudienceMessageGroup>() ?: AudienceMessageGroup.empty()
            return Impl(key, config, count, jumpedMessages)
        }
    }

    private class Impl(
        key: Key,
        config: ConfigurationNode,
        override val count: Int,
        override val jumpedMessages: AudienceMessageGroup
    ) : ExtraJump, AbilityBase(key, config) {
        override fun mechanic(input: AbilityInput): AbilityMechanic {
            return ExtraJumpAbilityMechanic(count, jumpedMessages)
        }
    }
}

private class ExtraJumpAbilityMechanic(
    private val count: Int,
    private val jumpedMessages: AudienceMessageGroup
) : AbilityMechanic() {
    private lateinit var jumpSubscription: Subscription
    private lateinit var touchGroundSubscription: Subscription

    private var jumpCount: Int = count

    override fun onEnable(componentMap: ComponentMap) = abilitySupport {
        jumpSubscription = Events.subscribe(PlayerInputEvent::class.java)
            .filter { it.player == componentMap.castByEntity() }
            .filter { jumpCount > 0 }
            .filter { it.input.isJump }
            .filter { !it.player.isOnGround } // Spigot 弃用此方法的原因是客户端可以通过发包来欺骗服务器, 我们这里不考虑挂端欺骗的情况.
            .handler {
                // 进行额外的跳跃效果, 也就是让玩家在跳跃的时候额外的向上移动一段距禽.
                val player = it.player
                player.velocity = player.velocity.setY(0.6)
                // 减少跳跃次数.
                jumpCount--

                // 播放粒子特效.
                playParticle(player, componentMap)

                // 发送跳跃消息.
                jumpedMessages.send(player)
            }

        touchGroundSubscription = Events.subscribe(PlayerMoveEvent::class.java)
            .filter { it.player == componentMap.castByEntity() }
            .filter { it.player.isOnGround }
            .handler {
                // 重置跳跃状态.
                jumpCount = count
            }
    }

    override fun onDisable(componentMap: ComponentMap) {
        jumpSubscription.unregister()
        touchGroundSubscription.unregister()
    }

    private fun playParticle(player: Player, componentMap: ComponentMap) = abilitySupport {
        // 设置粒子特效
        componentMap += ParticleEffectComponent(
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
    }

}