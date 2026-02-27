package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.util.adventure.BukkitSound
import net.kyori.adventure.key.Key
import org.bukkit.Sound
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class RandomTeleport(
    val searchRadius: Double = 128.0,
    val searchHeight: Double = 256.0,
    val allowedServers: Set<String> = setOf(),
    val allowedDimensions: Set<Key> = setOf(),
    val startingSound: Sound = BukkitSound.BLOCK_PORTAL_TRAVEL,
    val successSound: Sound = BukkitSound.ENTITY_ENDERMAN_TELEPORT,
    val failureSound: Sound = BukkitSound.ENTITY_ENDERMAN_HURT,
)