package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.behavior.impl.Arrow
import cc.mewcraft.wakame.item2.behavior.impl.HoldLastDamage
import cc.mewcraft.wakame.registry2.KoishRegistries2

/**
 * 所有的物品行为类型.
 */
object ItemBehaviorTypes {

    // ------------
    // 注册表
    // ------------

    /**
     * 将物品作为自定义箭矢的逻辑.
     */
    val ARROW = register("arrow", Arrow)

    /**
     * 使物品耐久耗尽进入“损坏状态”而不是消失的逻辑.
     */
    val HOLD_LAST_DAMAGE = register("hold_last_damage", HoldLastDamage)

    // ------------
    // 方便函数
    // ------------

    private fun register(id: String, type: ItemBehavior): ItemBehavior {
        KoishRegistries2.ITEM_BEHAVIOR.add(id, type)
        return type
    }

}