package cc.mewcraft.wakame.ability.combo.display

import net.kyori.adventure.text.format.Style
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class TriggerDisplay(
    val name: String,
    @Setting("success_style")
    val successStyle: Style,
    @Setting("progress_style")
    val progressStyle: Style,
    @Setting("failure_style")
    val failureStyle: Style,
)
