package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.resource.ResourceTypeRegistry
import cc.mewcraft.wakame.user.User
import team.unnamed.mocha.runtime.binding.Binding

internal class UserContext(
    private val user: User<*>
) {
    @Binding("max_mana")
    fun maxMana(): Double {
        return user.resourceMap.maximum(ResourceTypeRegistry.MANA).toDouble()
    }

    @Binding("mana")
    fun mana(): Double {
        return user.resourceMap.current(ResourceTypeRegistry.MANA).toDouble()
    }
}