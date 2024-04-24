package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity
import org.bukkit.Location as BukkitLocation
import org.bukkit.entity.LivingEntity as BukkitLivingEntity

sealed interface Target {
    interface Void : Target {

    }

    interface Location : Target {
        val bukkitLocation: BukkitLocation
    }

    interface LivingEntity : Target {
        val bukkitEntity: BukkitLivingEntity
    }

}

object TargetAdapter {
    fun adapt(user: User<*>): Target.LivingEntity {
        TODO()
    }

    fun adapt(entity: LivingEntity): Target.LivingEntity {
        return object : Target.LivingEntity {
            override val bukkitEntity: BukkitLivingEntity = entity
        }
    }

    fun adapt(location: BukkitLocation): Target.Location {
        return object : Target.Location {
            override val bukkitLocation: BukkitLocation = location
        }
    }
}