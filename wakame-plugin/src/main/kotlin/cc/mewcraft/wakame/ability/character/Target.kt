package cc.mewcraft.wakame.ability.character

import cc.mewcraft.wakame.user.User
import org.bukkit.entity.Player
import org.bukkit.Location as BukkitLocation
import org.bukkit.entity.LivingEntity as BukkitLivingEntity

sealed interface Target {

    val bukkitLocation: BukkitLocation

    val bukkitEntity: BukkitLivingEntity?

    interface Location : Target {
        override val bukkitLocation: BukkitLocation
    }

    interface LivingEntity : Target {
        override val bukkitEntity: BukkitLivingEntity?

        override val bukkitLocation: BukkitLocation
            get() = bukkitEntity?.location ?: throw IllegalStateException("Entity is null")
    }
}

object TargetAdapter {
    fun adapt(user: User<Player>): Target.LivingEntity {
        return BukkitLivingEntityTarget(user.player())
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