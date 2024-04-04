package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.user.User
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.LivingEntity as BukkitLivingEntity

sealed interface Target {
    interface Void : Target

    interface Location: Target {

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
        TODO()
    }

    fun adapt(location: Location): Target.Location {
        TODO()
    }
}