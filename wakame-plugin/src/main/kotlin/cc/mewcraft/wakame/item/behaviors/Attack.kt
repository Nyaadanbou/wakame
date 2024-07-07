package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

/**
 * 物品发动攻击的逻辑
 * 用于自定义的攻击冷却
 */
interface Attack : ItemBehavior {
    private object Default : Attack

    companion object Type : ItemBehaviorType<Attack> {
        override fun create(): Attack {
            return Default
        }
    }
}