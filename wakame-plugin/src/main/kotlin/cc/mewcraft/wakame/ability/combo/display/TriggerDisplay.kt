package cc.mewcraft.wakame.ability.combo.display

import net.kyori.adventure.text.format.Style
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class TriggerDisplay(
    @Required
    val name: String,
    @Required
    @Setting("success_style")
    val successStyle: Style,
    @Required
    @Setting("progress_style")
    val progressStyle: Style,
    @Required
    @Setting("failure_style")
    val failureStyle: Style,
)
