package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player
import team.unnamed.mocha.runtime.binding.Binding
import java.lang.ref.WeakReference

internal class UserContext(
    user: User<*>
) {
    private val weakUser: WeakReference<User<*>> = WeakReference(user)

    private val user: User<*>
        get() = weakUser.get() ?: throw IllegalStateException("User is not available")

    @Binding("max_health")
    fun maxHealth(): Double {
        return user.attributeMap.getInstance(Attributes.MAX_HEALTH)?.getValue() ?: 0.0
    }

    @Binding("health")
    fun health(): Double {
        val player = user.player
        if (player !is Player)
            return 0.0
        return player.health
    }

    @Binding("max_mana")
    fun maxMana(): Double {
        return user.resourceMap.maximum(ResourceTypeRegistry.MANA).toDouble()
    }

    @Binding("mana")
    fun mana(): Double {
        return user.resourceMap.current(ResourceTypeRegistry.MANA).toDouble()
    }
}