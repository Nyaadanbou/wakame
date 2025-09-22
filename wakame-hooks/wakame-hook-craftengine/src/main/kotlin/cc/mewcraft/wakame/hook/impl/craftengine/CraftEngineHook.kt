package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CEHookPlaceBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CEHookPlaceDoubleHighBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CEHookPlaceLiquidCollisionBlock
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.behavior.CustomBlockChecker
import cc.mewcraft.wakame.item2.behavior.impl.hook.craftengine.PlaceBlock
import cc.mewcraft.wakame.item2.behavior.impl.hook.craftengine.PlaceDoubleHighBlock
import cc.mewcraft.wakame.item2.behavior.impl.hook.craftengine.PlaceLiquidCollisionBlock
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks
import org.bukkit.block.Block

@Hook(plugins = ["CraftEngine"])
object CraftEngineHook {
    init {
        // 注册 CraftEngine 带来的新物品行为
        PlaceBlock.register(CEHookPlaceBlock)
        PlaceLiquidCollisionBlock.register(CEHookPlaceLiquidCollisionBlock)
        PlaceDoubleHighBlock.register(CEHookPlaceDoubleHighBlock)

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
