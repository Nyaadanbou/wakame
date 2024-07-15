package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.user.User
import team.unnamed.mocha.runtime.binding.Binding
import java.lang.ref.WeakReference

internal class UserContext(
    user: User<*>
) {
    private val weakUser: WeakReference<User<*>> = WeakReference(user)

    private val user: User<*>
        get() = weakUser.get() ?: throw IllegalStateException("User is not available")

    @Binding("max_mana")
    fun maxMana(): Double {
        return user.resourceMap.maximum(ResourceTypeRegistry.MANA).toDouble()
    }

    @Binding("mana")
    fun mana(): Double {
        return user.resourceMap.current(ResourceTypeRegistry.MANA).toDouble()
    }
}