package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.item.behaviors.*

/**
 * 所有的物品行为类型.
 */
object ItemBehaviorTypes {
    /**
     * 物品发动攻击的逻辑.
     */
    val ATTACK: ItemBehaviorType<Attack> = Attack

    /**
     * 物品施放技能的逻辑.
     */
    val CASTABLE: ItemBehaviorType<Castable> = Castable

    /**
     * 物品充能的逻辑.
     */
    val CHARGEABLE: ItemBehaviorType<Chargeable> = Chargeable

    /**
     * 物品受损的逻辑.
     */
    val DAMAGEABLE: ItemBehaviorType<Damageable> = Damageable

    /**
     * 物品附魔的逻辑.
     */
    val ENCHANTABLE: ItemBehaviorType<Enchantable> = Enchantable

    /**
     * 可食用物品的逻辑.
     */
    val FOOD: ItemBehaviorType<Food> = Food

    /**
     * 当玩家的冒险等级 < 物品等级时, 禁用物品的逻辑.
     */
    val LEVEL_BARRIER: ItemBehaviorType<LevelBarrier> = LevelBarrier

    /**
     * 物品作为工具的逻辑.
     */
    val TOOL: ItemBehaviorType<Tool> = Tool

    /**
     * 物品记录数据的逻辑.
     */
    val TRACKABLE: ItemBehaviorType<Trackable> = Trackable

    /**
     * 物品穿戴的逻辑.
     */
    val WEARABLE: ItemBehaviorType<Wearable> = Wearable
}