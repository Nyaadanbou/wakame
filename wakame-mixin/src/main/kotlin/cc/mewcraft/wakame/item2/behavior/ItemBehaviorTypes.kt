package cc.mewcraft.wakame.item2.behavior

import cc.mewcraft.wakame.item2.behavior.impl.*

/**
 * 所有的物品行为类型.
 */
object ItemBehaviorTypes {

    /**
     * 物品作为自定义箭矢的逻辑.
     */
    val ARROW: ItemBehavior = Arrow

    /**
     * 物品发动攻击的逻辑.
     */
    val ATTACK: ItemBehavior = Attack

    /**
     * 物品施放技能的逻辑.
     */
    val CASTABLE: ItemBehavior = Castable

    /**
     * 物品耐久耗尽进入“损坏状态”而不是消失的逻辑.
     */
    val HOLD_LAST_DAMAGE: ItemBehavior = HoldLastDamage

    /**
     * 当玩家的冒险等级 < 物品等级时, 禁用物品的逻辑.
     */
    val LEVEL_BARRIER: ItemBehavior = LevelBarrier

    /**
     * 消耗后给予限时飞行的逻辑.
     */
    val TOWN_FLIGHT: ItemBehavior = TownFlight

    /**
     * 物品记录数据的逻辑.
     */
    val TRACKABLE: ItemBehavior = Trackable

    /**
     * 消耗后控制世界时间的逻辑.
     */
    val WORLD_TIME_CONTROL: ItemBehavior = WorldTimeControl

    /**
     * 消耗后控制世界天气的逻辑.
     */
    val WORLD_WEATHER_CONTROL: ItemBehavior = WorldWeatherControl

}