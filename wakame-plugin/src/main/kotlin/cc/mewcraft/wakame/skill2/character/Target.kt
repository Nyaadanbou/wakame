package cc.mewcraft.wakame.skill2.character

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity as BukkitLivingEntity
import org.bukkit.entity.Player
import org.bukkit.Location as BukkitLocation

sealed interface Target {

    val bukkitLocation: BukkitLocation

    interface Location : Target {
        override val bukkitLocation: BukkitLocation
    }

    interface LivingEntity : Target {
        val bukkitEntity: BukkitLivingEntity?

        override val bukkitLocation: BukkitLocation
            get() = bukkitEntity?.location ?: throw IllegalStateException("Entity is null")
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

    fun adapt(entity: BukkitLivingEntity): Target.LivingEntity {
        return BukkitLivingEntityTarget(entity)
    }

    fun adapt(location: BukkitLocation): Target.Location {
        return BukkitLocationTarget(location)
    }

    fun adapt(caster: Caster): Target {
        val entity = caster.entity as? BukkitLivingEntity ?: throw IllegalArgumentException("Caster must be a living entity")
        return BukkitLivingEntityTarget(entity)
    }
}