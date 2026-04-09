package cc.mewcraft.wakame.item.property.impl.weapon

import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 法杖的配置.
 *
 * @property isTwoHanded 是否需要双手持握才能攻击.
 */
@ConfigSerializable
data class Wand(
    override val isTwoHanded: Boolean,
) : ConfigurableTwoHanded