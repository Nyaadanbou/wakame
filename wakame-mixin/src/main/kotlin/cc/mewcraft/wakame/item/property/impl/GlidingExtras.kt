package cc.mewcraft.wakame.item.property.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class GlidingExtras(
    val glideDrainPerSecond: Double = 0.0,
    val enterGlideManaCost: Double = 0.0,
    val rocketBoostManaCost: Double = 0.0,
)