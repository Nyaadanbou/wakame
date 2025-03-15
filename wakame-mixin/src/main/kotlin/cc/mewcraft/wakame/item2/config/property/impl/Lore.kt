package cc.mewcraft.wakame.item2.config.property.impl

import net.kyori.adventure.text.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class Lore(
    @Setting(nodeFromParent = true)
    val lore: List<Component>,
)