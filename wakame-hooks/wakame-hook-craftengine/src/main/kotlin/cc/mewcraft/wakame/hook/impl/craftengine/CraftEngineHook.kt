package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceDoubleHighBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceLiquidCollisionBlock
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.behavior.CustomBlockChecker
import cc.mewcraft.wakame.item2.behavior.impl.external.PlaceBlock
import cc.mewcraft.wakame.item2.behavior.impl.external.PlaceDoubleHighBlock
import cc.mewcraft.wakame.item2.behavior.impl.external.PlaceLiquidCollisionBlock
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks
import org.bukkit.block.Block

@Hook(plugins = ["CraftEngine"])
object CraftEngineHook {

    init {

        // 注册 CraftEngine 带来的新物品行为实例
        PlaceBlock.register(CraftEnginePlaceBlock)
        PlaceDoubleHighBlock.register(CraftEnginePlaceDoubleHighBlock)
        PlaceLiquidCollisionBlock.register(CraftEnginePlaceLiquidCollisionBlock)

        // 修改 Koish 交互系统判定自定义方块的方法
        CustomBlockChecker.register(CraftEngineCustomBlockChecker)
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
