package cc.mewcraft.wakame.item.behavior

import cc.mewcraft.wakame.item.behaviors.Attributable
import cc.mewcraft.wakame.item.behaviors.Castable
import cc.mewcraft.wakame.item.behaviors.Chargeable
import cc.mewcraft.wakame.item.behaviors.Damageable
import cc.mewcraft.wakame.item.behaviors.Enchantable
import cc.mewcraft.wakame.item.behaviors.Food
import cc.mewcraft.wakame.item.behaviors.Kizamiable
import cc.mewcraft.wakame.item.behaviors.Tool
import cc.mewcraft.wakame.item.behaviors.Trackable
import cc.mewcraft.wakame.item.behaviors.Wearable

object ItemBehaviorTypes {
    val ATTRIBUTABLE: ItemBehaviorType<Attributable> = Attributable
    val CASTABLE: ItemBehaviorType<Castable> = Castable
    val CHARGEABLE: ItemBehaviorType<Chargeable> = Chargeable
    val DAMAGEABLE: ItemBehaviorType<Damageable> = Damageable
    val ENCHANTABLE: ItemBehaviorType<Enchantable> = Enchantable
    val FOOD: ItemBehaviorType<Food> = Food
    val KIZAMIABLE: ItemBehaviorType<Kizamiable> = Kizamiable
    val TOOL: ItemBehaviorType<Tool> = Tool
    val TRACKABLE: ItemBehaviorType<Trackable> = Trackable
    val WEARABLE: ItemBehaviorType<Wearable> = Wearable
}