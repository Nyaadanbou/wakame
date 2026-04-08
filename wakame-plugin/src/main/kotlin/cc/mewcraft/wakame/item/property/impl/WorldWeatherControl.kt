package cc.mewcraft.wakame.item.property.impl

import cc.mewcraft.wakame.world.WeatherControl
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class WorldWeatherControl(
    @Setting(nodeFromParent = true)
    val actions: List<WeatherControl.Action>
)
