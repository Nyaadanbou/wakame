package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.util.KoishKey
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class LiquidCollisionBlockSettings(
    val blockId: KoishKey,
    val offset: Int = 1,
)