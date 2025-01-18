package cc.mewcraft.wakame.ability.display

import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class AbilityDisplay(
    val name: String = "",
    val tooltips: List<String> = emptyList(),
)
