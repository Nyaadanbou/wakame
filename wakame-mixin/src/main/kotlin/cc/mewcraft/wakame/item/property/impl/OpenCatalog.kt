package cc.mewcraft.wakame.item.property.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting


@ConfigSerializable
data class OpenCatalog(
    @Setting("type")
    val catalogType: String,
    @Setting("id")
    val catalogId: String,
)