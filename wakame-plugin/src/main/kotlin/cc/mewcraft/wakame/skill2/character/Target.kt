package cc.mewcraft.wakame.skill2.character

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity as BukkitLivingEntity
import org.bukkit.entity.Player
import org.bukkit.Location as BukkitLocation

sealed interface Target {
    data object Void : Target {
        override fun <T : Target> value(clazz: Class<T>): T? = null
        override fun <T : Target> valueNonNull(clazz: Class<T>): T = throw IllegalStateException("No value")
    }

    interface Location : Target {
        val bukkitLocation: BukkitLocation
    }

    interface LivingEntity : Target {
        val bukkitEntity: BukkitLivingEntity?
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

    fun adapt(caster: Caster.Single): Target {
        when (caster) {
            is Caster.Single.Entity -> return adapt(caster.bukkitEntity as BukkitLivingEntity)
            is Caster.Single.Skill -> throw UnsupportedOperationException()
        }
    }

    fun adapt(caster: Caster): Target {
        val single = caster.value<Caster.Single>()
            ?: caster.root<Caster.Single>()
            ?: throw IllegalStateException("No single caster")
        return adapt(single)
    }
}