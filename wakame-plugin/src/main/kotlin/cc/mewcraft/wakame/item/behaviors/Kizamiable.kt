package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

/**
 * 物品提供铭刻加成的逻辑.
 */
@Deprecated("不再使用")
interface Kizamiable : ItemBehavior {
    private object Default : Kizamiable

    companion object Type : ItemBehaviorType<Kizamiable> {
        override fun create(): Kizamiable {
            return Default
        }
    }
}