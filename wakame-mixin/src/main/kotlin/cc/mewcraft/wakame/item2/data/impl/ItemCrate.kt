package cc.mewcraft.wakame.item2.data.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable

// TODO: 完成盲盒内容
@ConfigSerializable
data class ItemCrate(
    /**
     * 盲盒的等级.
     *
     * 盲盒的等级决定了盲盒的内容.
     */
    val level: Int,
)
