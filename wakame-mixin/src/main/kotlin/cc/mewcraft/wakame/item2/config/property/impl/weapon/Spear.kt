package cc.mewcraft.wakame.item2.config.property.impl.weapon

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 长矛的配置.
 */
@ConfigSerializable
data class Spear(
    val size: Float = .2f,
    override val cancelVanillaDamage: Boolean = false,
) : WeaponBase()
