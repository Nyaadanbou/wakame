package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.hook.impl.craftengine.block.CraftEngineUniversalBlocks
import cc.mewcraft.wakame.hook.impl.craftengine.item.CraftEngineItemRefHandler
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceDoubleHighBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceLiquidCollisionBlock
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceBlock
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceDoubleHighBlock
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceLiquidCollisionBlock
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.util.UniversalBlocks

@Hook(plugins = ["CraftEngine"])
object CraftEngineHook {

    init {
        // 修改实现为 CraftEngine 提供的版本
        PlaceBlock.setImplementation(CraftEnginePlaceBlock)
        PlaceDoubleHighBlock.setImplementation(CraftEnginePlaceDoubleHighBlock)
        PlaceLiquidCollisionBlock.setImplementation(CraftEnginePlaceLiquidCollisionBlock)
        UniversalBlocks.setImplementation(CraftEngineUniversalBlocks)

        // 向 Koish 注册 CraftEngine 物品
        BuiltInRegistries.ITEM_REF_HANDLER_EXTERNAL.add("craftengine", CraftEngineItemRefHandler())
    }
}
