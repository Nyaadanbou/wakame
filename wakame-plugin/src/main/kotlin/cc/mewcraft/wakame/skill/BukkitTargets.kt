@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.skill

import org.bukkit.Location
import org.bukkit.entity.LivingEntity

data class BukkitLocationTarget(
    override val bukkitLocation: Location
) : Target.Location {
    override fun <T : Target> value(clazz: Class<T>): T? {
        return when (clazz) {
            Target.Location::class.java -> this as T
            else -> null
        }
    }

    override fun <T : Target> valueNonNull(clazz: Class<T>): T {
        return requireNotNull(value(clazz)) { "Target is not $clazz" }
    }
}

data class BukkitLivingEntityTarget(
    override val bukkitEntity: LivingEntity
) : Target.LivingEntity {
    override fun <T : Target> value(clazz: Class<T>): T? {
        return when (clazz) {
            Target.LivingEntity::class.java -> this as T
            else -> null
        }
    }

    override fun <T : Target> valueNonNull(clazz: Class<T>): T {
        return requireNotNull(value(clazz)) { "Target is not $clazz" }
    }
}