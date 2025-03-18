package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.behavior.impl.Arrow
import cc.mewcraft.wakame.item2.behavior.impl.HoldLastDamage
import cc.mewcraft.wakame.registry2.KoishRegistries2

/**
 * 该 `object` 包含了所有可用的 [ItemBehavior].
 *
 * [ItemBehavior] 描述的是一个物品与世界交互的逻辑, 不包含任何数据.
 */
data object ItemBehaviorTypes {

    // ------------
    // 注册表
    // ------------

    /**
     * 将物品作为自定义箭矢的逻辑.
     */
    val ARROW = typeOf("arrow", Arrow)

    /**
     * 使物品耐久耗尽后进入“损坏状态”而非直接消失的逻辑.
     */
    val HOLD_LAST_DAMAGE = typeOf("hold_last_damage", HoldLastDamage)

    // ------------
    // 方便函数
    // ------------

    private fun typeOf(id: String, type: ItemBehavior): ItemBehavior {
        return type.also { KoishRegistries2.ITEM_BEHAVIOR.add(id, it) }
    }

}