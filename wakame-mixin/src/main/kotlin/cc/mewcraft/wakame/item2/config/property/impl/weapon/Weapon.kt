package cc.mewcraft.wakame.item2.config.property.impl.weapon

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * “一般的”的武器配置.
 */
@ConfigSerializable
data class Weapon(
    override val cancelVanillaDamage: Boolean = false,
) : WeaponBase()