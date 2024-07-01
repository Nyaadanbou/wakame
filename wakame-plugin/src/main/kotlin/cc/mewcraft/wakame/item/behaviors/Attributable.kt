package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

/**
 * 物品提供属性加成的逻辑.
 */
@Deprecated("不再使用")
interface Attributable : ItemBehavior {
    private object Default : Attributable

    companion object Type : ItemBehaviorType<Attributable> {
        override fun create(): Attributable {
            return Default
        }
    }
}