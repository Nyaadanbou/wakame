package cc.mewcraft.wakame.item2.config.property.impl.hook.craftengine

import cc.mewcraft.wakame.util.Identifier
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class LiquidCollisionBlockSettings(
    val blockId: Identifier,
    val offset: Int = 1
)
