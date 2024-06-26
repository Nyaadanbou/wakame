package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory

/**
 * 物品提供铭刻加成的逻辑.
 */
interface Kizamiable : ItemBehavior {
    private object Default : Kizamiable

    companion object Factory : ItemBehaviorFactory<Kizamiable> {
        override fun create(): Kizamiable {
            return Default
        }
    }
}