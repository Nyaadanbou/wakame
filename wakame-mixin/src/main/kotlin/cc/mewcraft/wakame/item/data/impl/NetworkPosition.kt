package cc.mewcraft.wakame.item.data.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class NetworkPosition(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val world: String,
    val server: String,
)