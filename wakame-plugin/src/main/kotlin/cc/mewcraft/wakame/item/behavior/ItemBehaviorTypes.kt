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
    val ATTRIBUTABLE: Attributable = Attributable.create()
    val CASTABLE: Castable = Castable.create()
    val CHARGEABLE: Chargeable = Chargeable.create()
    val DAMAGEABLE: Damageable = Damageable.create()
    val ENCHANTABLE: Enchantable = Enchantable.create()
    val FOOD: Food = Food.create()
    val KIZAMIABLE: Kizamiable = Kizamiable.create()
    val TOOL: Tool = Tool.create()
    val TRACKABLE: Trackable = Trackable.create()
    val WEARABLE: Wearable = Wearable.create()
}