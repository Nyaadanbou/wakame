package cc.mewcraft.wakame.item.property.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class TownyFlight(
    val duration: Long, // seconds
    val rocketOnConsume: Boolean,
    val rocketForce: Double,
) {

    init {
        require(duration > 0) { "duration (seconds) must be greater than 0" }
    }
}