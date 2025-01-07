@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.factory.implement

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.AbilityMechanic
import cc.mewcraft.wakame.ability.context.AbilityInput
import cc.mewcraft.wakame.ability.factory.AbilityFactory
import cc.mewcraft.wakame.ability.factory.abilitySupport
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.Events
import me.lucko.helper.event.Subscription
import net.kyori.adventure.key.Key
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.spongepowered.configurate.ConfigurationNode

interface ExtraJump : Ability {
    val count: Int

    companion object Factory : AbilityFactory<ExtraJump> {
        override fun create(key: Key, config: ConfigurationNode): ExtraJump {
            val count = config.node("count").krequire<Int>()
            return Impl(key, config, count)
        }
    }

    private class Impl(
        key: Key,
        config: ConfigurationNode,
        override val count: Int,
    ) : ExtraJump, AbilityBase(key, config) {
        override fun mechanic(input: AbilityInput): AbilityMechanic {
            return ExtraJumpAbilityMechanic(count)
        }
    }
}

private class ExtraJumpAbilityMechanic(
    private val count: Int,
) : AbilityMechanic() {
    private lateinit var jumpSubscription: Subscription
    private lateinit var touchGroundSubscription: Subscription

    private var jumpCount: Int = count
    private var lastJump: Double = 0.0

    override fun onEnable(componentMap: ComponentMap) = abilitySupport {
        jumpSubscription = Events.subscribe(PlayerInputEvent::class.java)
            .filter { it.player == componentMap.castByEntity() }
            .filter { it.input.isJump }
            .filter { !it.player.isJumping }
            .filter { jumpCount > 0 }
            .handler {
                // 进行额外的跳跃效果, 也就是让玩家在跳跃的时候额外的向上移动一段距禽.
                it.player.velocity = it.player.velocity.setY(0.5)
                // 记录最后一次跳跃的时间.
                lastJump = componentMap.tickCount ?: 0.0
                // 减少跳跃次数.
                jumpCount--
            }

        touchGroundSubscription = Events.subscribe(PlayerMoveEvent::class.java)
            .filter { it.player == componentMap.castByEntity() }
            .filter { it.player.isJumping }
            .handler {
                // 重置跳跃状态.
                jumpCount = count
            }
    }

    override fun onDisable(componentMap: ComponentMap) {
        jumpSubscription.unregister()
        touchGroundSubscription.unregister()
    }
}