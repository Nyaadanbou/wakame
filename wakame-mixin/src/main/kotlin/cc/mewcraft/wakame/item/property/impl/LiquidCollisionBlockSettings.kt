package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.util.Identifier
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class LiquidCollisionBlockSettings(
    val blockId: Identifier,
    val offset: Int = 1,
)