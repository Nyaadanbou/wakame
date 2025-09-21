package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl.PlaceBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl.PlaceDoubleHighBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl.PlaceLiquidCollisionBlock
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item2.config.property.ItemPropTypes
import cc.mewcraft.wakame.util.Identifier

@Hook(plugins = ["CraftEngine"])
object CraftEngineHook {
    init {
        // 触发子 object 的初始化
        ExtraItemPropTypes
        ExtraItemBehaviorTypes
    }

    object ExtraItemPropTypes {
        val PLACE_BLOCK = ItemPropTypes.register<Identifier>("place_block")
        val PLACE_LIQUID_COLLISION_BLOCK = ItemPropTypes.register<Identifier>("place_liquid_collision_block")
        val PLACE_DOUBLE_HIGH_BLOCK = ItemPropTypes.register<Identifier>("place_double_high_block")
    }

    object ExtraItemBehaviorTypes {
        val PLACE_BLOCK = ItemBehaviorTypes.register("place_block", PlaceBlock)
        val PLACE_LIQUID_COLLISION_BLOCK = ItemBehaviorTypes.register("place_liquid_collision_block", PlaceLiquidCollisionBlock)
        val PLACE_DOUBLE_HIGH_BLOCK = ItemBehaviorTypes.register("place_double_high_block", PlaceDoubleHighBlock)
    }
}
