package cc.mewcraft.wakame.item2.data.impl

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.util.Identifier
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * 用于识别一个自定义物品的类型, 即 [KoishItem].
 */
@ConfigSerializable
data class ItemId(
    val id: Identifier,
    val variant: Int = 0,
) {

    val type: KoishItem?


}
