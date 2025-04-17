package cc.mewcraft.wakame.ability2.combo.display

import net.kyori.adventure.text.format.Style
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class TriggerDisplay(
    val name: String,
    val successStyle: Style,
    val progressStyle: Style,
    val failureStyle: Style,
)
