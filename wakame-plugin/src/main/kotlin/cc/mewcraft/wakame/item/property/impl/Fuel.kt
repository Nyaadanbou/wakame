package cc.mewcraft.wakame.item.property.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class Fuel(
    val burnTime: Int = 20,
    val consume: Boolean = true,
)