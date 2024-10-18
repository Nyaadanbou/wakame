package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.user.UserManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Provides the access to the [AttributeMap] of all (online) players.
 */
data object PlayerAttributeMapAccess : KoinComponent, AttributeMapAccess<Player> {
    private val userManager: UserManager<Player> by inject()

    override fun get(subject: Player): AttributeMap {
        // 该实现仅仅把对函数的调用转发到对应的 User 实例之下
        return userManager.getPlayer(subject).attributeMap
    }
}

/**
 * Provides the access to [AttributeMap] of all non-player entities.
 */
data object EntityAttributeMapAccess : AttributeMapAccess<LivingEntity> {
    override fun get(subject: LivingEntity): AttributeMap {
        return AttributeMap(subject)
    }
}