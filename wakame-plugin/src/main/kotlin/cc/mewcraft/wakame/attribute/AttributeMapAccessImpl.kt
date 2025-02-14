package cc.mewcraft.wakame.attribute

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

/**
 * Provides the access to the [AttributeMap] of a specific subject.
 */
internal data object AttributeMapAccessImpl : AttributeMapAccess {
    override fun get(subject: Any): Result<AttributeMap> = runCatching {
        when (subject) {
            is Player -> AttributeMap(subject)
            is LivingEntity -> AttributeMap(subject)
            else -> throw IllegalArgumentException("Unsupported subject type: ${subject::class.simpleName}")
        }
    }
}