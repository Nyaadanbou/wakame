package cc.mewcraft.wakame.skill

import org.bukkit.Location
import org.bukkit.entity.LivingEntity

data class BukkitLocationTarget(
    override val bukkitLocation: Location
) : Target.Location

data class BukkitLivingEntityTarget(
    override val bukkitEntity: LivingEntity
): Target.LivingEntity