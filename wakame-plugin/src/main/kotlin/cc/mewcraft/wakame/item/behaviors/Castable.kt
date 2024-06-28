package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

/**
 * 物品施放技能的逻辑.
 *
 * 读取物品的技能信息, 然后施放技能.
 */
interface Castable : ItemBehavior {
    private object Default : Castable

    companion object Type : ItemBehaviorType<Castable> {
        override fun create(): Castable {
            return Default
        }
    }
}