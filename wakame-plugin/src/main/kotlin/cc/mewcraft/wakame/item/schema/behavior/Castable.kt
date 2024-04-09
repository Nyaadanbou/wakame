package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.schema.NekoItem

/**
 * 可以施放技能的物品。
 *
 * 读取物品的技能信息，然后施放技能。
 */
interface Castable : ItemBehavior {
    companion object Factory : ItemBehaviorFactory<Castable> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): Castable {
            TODO("Not yet implemented")
        }
    }
}