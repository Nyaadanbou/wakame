package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.item.behaviors.*

/**
 * 所有的物品行为类型.
 */
object ItemBehaviorTypes {
    /**
     * 物品作为自定义箭矢的逻辑.
     */
    val ARROW: ItemBehaviorType<Arrow> = Arrow

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

    // FIXME #363: 临时代码, 仅用于测试
    val DOUBLE_ATTACK: ItemBehaviorType<DoubleAttack> = DoubleAttack

    /**
     * 物品附魔的逻辑.
     */
    val ENCHANTABLE: ItemBehaviorType<Enchantable> = Enchantable

    /**
     * 可食用物品的逻辑.
     */
    val FOOD: ItemBehaviorType<Food> = Food

    /**
     * 物品耐久耗尽进入“损坏状态”而不是消失的逻辑.
     */
    val HOLD_LAST_DAMAGE: ItemBehaviorType<HoldLastDamage> = HoldLastDamage

    /**
     * 当玩家的冒险等级 < 物品等级时, 禁用物品的逻辑.
     */
    val LEVEL_BARRIER: ItemBehaviorType<LevelBarrier> = LevelBarrier

    /**
     * 物品作为工具的逻辑.
     */
    val TOOL: ItemBehaviorType<Tool> = Tool

    /**
     * 消耗后给予限时飞行的逻辑.
     */
    val TOWN_FLIGHT: ItemBehaviorType<TownFlight> = TownFlight

    /**
     * 物品记录数据的逻辑.
     */
    val TRACKABLE: ItemBehaviorType<Trackable> = Trackable

    /**
     * 物品穿戴的逻辑.
     */
    val WEARABLE: ItemBehaviorType<Wearable> = Wearable

    /**
     * 消耗后控制世界时间的逻辑.
     */
    val WORLD_TIME_CONTROL: ItemBehaviorType<WorldTimeControl> = WorldTimeControl

    /**
     * 消耗后控制世界天气的逻辑.
     */
    val WORLD_WEATHER_CONTROL: ItemBehaviorType<WorldWeatherControl> = WorldWeatherControl
}