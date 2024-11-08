package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.user.UserManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Provides the access to the [AttributeMap] of a specific subject.
 */
data object AttributeMapAccessImpl : KoinComponent, AttributeMapAccess {
    private val userManager: UserManager<Player> by inject()

    override fun get(subject: Any): Result<AttributeMap> {
        return runCatching {
            when (subject) {
                is Player -> userManager.getUser(subject).attributeMap
                is LivingEntity -> AttributeMap(subject)
                else -> throw IllegalArgumentException("Unsupported subject type: ${subject::class.simpleName}")
            }
        }
    }
}