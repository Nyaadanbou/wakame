package cc.mewcraft.wakame.item2.data.impl

import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishItemProxy
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifier
import org.spongepowered.configurate.objectmapping.ConfigSerializable

// FIXME #350: 对象池
// FIXME #350: 对于 KoinItemProxy 也需要写入这个数据以提高查询性能
/**
 * 用于识别一个自定义物品的类型.
 */
@ConfigSerializable
data class ItemId(
    val id: Identifier,
    val variant: Int = 0,
) {

    @Transient // FIXME #350: 好像构造器之外的 property 本就不会参与序列化
    val itemType: KoishItem? = KoishRegistries2.ITEM[id]
    @Transient
    val itemProxy: KoishItemProxy? = KoishRegistries2.ITEM_PROXY[id]

}
