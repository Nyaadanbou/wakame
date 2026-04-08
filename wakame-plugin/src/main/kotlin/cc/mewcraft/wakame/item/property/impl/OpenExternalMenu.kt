package cc.mewcraft.wakame.item.property.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting


@ConfigSerializable
data class OpenExternalMenu(
    @Setting("id")
    val menuId: String,
    @Setting("args")
    val menuArgs: List<String> = emptyList(),
)
