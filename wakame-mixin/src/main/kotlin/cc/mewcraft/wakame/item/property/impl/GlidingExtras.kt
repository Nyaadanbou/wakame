package cc.mewcraft.wakame.item.property.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class GlidingExtras(
    val glideDrainPerSecond: Double = 1.0,
    val enterGlideManaCost: Double = 2.0,
    val rocketBoostManaCost: Double = 3.0,
)