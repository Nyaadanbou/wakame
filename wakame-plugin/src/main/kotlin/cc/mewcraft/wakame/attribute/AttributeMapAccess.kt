package cc.mewcraft.wakame.attribute

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

/**
 * Provides the access to the [AttributeMap] of a specific subject.
 */
data object DefaultAttributeMapAccess : KoinComponent, AttributeMapAccess {
    override fun get(subject: Any): Result<AttributeMap> {
        return runCatching {
            when (subject) {
                is Player -> AttributeMap(subject)
                is LivingEntity -> AttributeMap(subject)
                else -> throw IllegalArgumentException("unsupported subject type: ${subject::class.simpleName}")
            }
        }
    }
}