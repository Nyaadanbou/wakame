package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.Location as BukkitLocation
import org.bukkit.entity.LivingEntity as BukkitLivingEntity

sealed interface Target {
    data object Void : Target {
        override fun <T : Target> value(clazz: Class<T>): T? = null
        override fun <T : Target> valueNonNull(clazz: Class<T>): T = throw IllegalStateException("No value")
    }

    interface Location : Target {
        val bukkitLocation: BukkitLocation
    }

    interface LivingEntity : Target {
        val bukkitEntity: BukkitLivingEntity
    }

    fun <T : Target> value(clazz: Class<T>): T?
    fun <T : Target> valueNonNull(clazz: Class<T>): T
}

inline fun <reified T : Target> Target.value(): T? = value(T::class.java)
inline fun <reified T : Target> Target.valueNonNull(): T = valueNonNull(T::class.java)

object TargetAdapter {
    fun adapt(user: User<Player>): Target.LivingEntity {
        return BukkitLivingEntityTarget(user.player)
    }

    fun adapt(entity: LivingEntity): Target.LivingEntity {
        return BukkitLivingEntityTarget(entity)
    }

    fun adapt(location: BukkitLocation): Target.Location {
        return BukkitLocationTarget(location)
    }
}