package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.Location as BukkitLocation
import org.bukkit.entity.LivingEntity as BukkitLivingEntity

sealed interface Target {
    data object Void : Target

    interface Location : Target {
        val bukkitLocation: BukkitLocation
    }

    interface LivingEntity : Target {
        val bukkitEntity: BukkitLivingEntity
    }

}

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