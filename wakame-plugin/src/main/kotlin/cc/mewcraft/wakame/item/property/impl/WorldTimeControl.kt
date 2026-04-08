package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.world.TimeControl
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class WorldTimeControl(
    val type: TimeControl.ActionType,
    val time: Long,
) {

    init {
        require(time >= 0) { "time must be non-negative" }
    }
}