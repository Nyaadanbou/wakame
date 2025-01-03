package cc.mewcraft.wakame.registries

import cc.mewcraft.wakame.core.WritableRegistry
import cc.mewcraft.wakame.item.NekoItem

// 属于曲线救国范畴内的注册表
object VolatileRegistries {
    // [原版套皮物品]
    // 玩家无法直接获得/使用, 仅用于给纯原版物品套一层皮 (i.e., 给原版物品添加内容)
    @JvmField
    val VANILLA_PROXY_ITEM: WritableRegistry<NekoItem> = TODO()
}