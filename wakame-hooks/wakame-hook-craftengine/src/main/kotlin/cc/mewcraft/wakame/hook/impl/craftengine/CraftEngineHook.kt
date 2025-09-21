package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl.PlaceBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl.PlaceDoubleHighBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.impl.PlaceLiquidCollisionBlock
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.behavior.CustomBlockChecker
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item2.config.property.ItemPropTypes
import cc.mewcraft.wakame.util.Identifier
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks
import org.bukkit.block.Block

@Hook(plugins = ["CraftEngine"])
object CraftEngineHook {
    init {
        // 触发子 object 的初始化
        ExtraItemPropTypes
        ExtraItemBehaviorTypes

        // 修改 Koish 交互系统判定自定义方块的方法
        CustomBlockChecker.register(CraftEngineCustomBlockChecker)
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

    /**
     * CraftEngine 的自定义方块检查器.
     */
    object CraftEngineCustomBlockChecker : CustomBlockChecker {
        override fun isCustomBlock(block: Block): Boolean {
            return CraftEngineBlocks.isCustomBlock(block)
        }
    }
}
