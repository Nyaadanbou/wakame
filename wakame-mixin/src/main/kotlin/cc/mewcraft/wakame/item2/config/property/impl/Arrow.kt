package cc.mewcraft.wakame.item2.config.property.impl

import org.bukkit.entity.AbstractArrow.PickupStatus
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class Arrow(
    /**
     * 可穿透的实体数.
     */
    val pierceLevel: Int,
    /**
     * 箭是否能被捡起.
     */
    val pickupStatus: PickupStatus,
    /**
     * 箭矢本身的着火时间.
     */
    val fireTicks: Int,
    /**
     * 击中实体造成的着火时间.
     */
    val hitFireTicks: Int,
    /**
     * 击中实体造成的冰冻时间, 实现用的是原版细雪的冰冻效果.
     * 当实体在细雪中时每刻增加`1`, 离开细雪则每刻减少`2`.
     */
    val hitFrozenTicks: Int,
    /**
     * 击中实体造成的发光时间. 仅在箭矢是光灵箭时有效.
     */
    val glowTicks: Int,
)