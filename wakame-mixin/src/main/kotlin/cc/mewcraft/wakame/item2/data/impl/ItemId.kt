package cc.mewcraft.wakame.item2.data.impl

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.util.Identifier
import org.spongepowered.configurate.objectmapping.ConfigSerializable

// FIXME #350: 对于 KoinItemProxy 也需要写入这个数据以提高查询性能
/**
 * 用于识别一个自定义物品的类型, 即 [KoishItem].
 */
@ConfigSerializable
data class ItemId(
    val id: Identifier,
    val variant: Int = 0,
) {

    val koishItem: KoishItem?
        get() = TODO("#350: 根据 id 从 Registry 获取 KoishItem")

}
