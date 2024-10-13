package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.user.PlayerAdapter
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.UserManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Provides the access to [AttributeMap] of various subjects.
 *
 * See the document of the subtypes for more details of usage.
 */
sealed class AttributeAccessor<T> {
    /**
     * Gets the [AttributeMap] for the [subject].
     */
    abstract fun getAttributeMap(subject: T): AttributeMap
}

/**
 * Provides the access to the [AttributeMap] of all (online) players.
 *
 * To get the [AttributeMap] of a player, it's recommended using the
 * [PlayerAdapter] (instead of [PlayerAttributeAccessor]) to get the [User]
 * instance, from which you can get the [AttributeMap] for that player.
 */
data object PlayerAttributeAccessor : KoinComponent, AttributeAccessor<Player>() {
    private val userManager: UserManager<Player> by inject()

    override fun getAttributeMap(subject: Player): AttributeMap {
        // the implementation simply redirect calls to the UserManager
        return userManager.getPlayer(subject).attributeMap
    }
}

/**
 * Provides the access to [AttributeMap] of all non-player entities.
 */
data object EntityAttributeAccessor : AttributeAccessor<LivingEntity>() {
    override fun getAttributeMap(subject: LivingEntity): AttributeMap {
        return AttributeMap(subject)
    }
}