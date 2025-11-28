package cc.mewcraft.wakame.hook.impl.craftengine

import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceDoubleHighBlock
import cc.mewcraft.wakame.hook.impl.craftengine.item.behavior.CraftEnginePlaceLiquidCollisionBlock
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceBlock
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceDoubleHighBlock
import cc.mewcraft.wakame.item.behavior.impl.external.PlaceLiquidCollisionBlock
import cc.mewcraft.wakame.util.BlockUtils
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks
import org.bukkit.block.Block

@Hook(plugins = ["CraftEngine"])
object CraftEngineHook {

    init {

        // 注册 CraftEngine 带来的新物品行为实例
        PlaceBlock.register(CraftEnginePlaceBlock)
        PlaceDoubleHighBlock.register(CraftEnginePlaceDoubleHighBlock)
        PlaceLiquidCollisionBlock.register(CraftEnginePlaceLiquidCollisionBlock)

        // 修改 Koish BlockUtils中的方法
        BlockUtils.register(CraftEngineBlockUtils)
    }

    /**
     * Hook CraftEngine 后 [cc.mewcraft.wakame.util.BlockUtils] 的实现.
     */
    object CraftEngineBlockUtils : BlockUtils {
        override fun isCustomBlock(block: Block): Boolean {
            return CraftEngineBlocks.isCustomBlock(block)
        }

        override fun getBlockId(block: Block): Identifier {
            val immutableBlockState = CraftEngineBlocks.getCustomBlockState(block)
            if (immutableBlockState == null) {
                return block.type.key()
            } else {
                val ceKey = immutableBlockState.owner().value().id()
                return Identifiers.of(ceKey.namespace, ceKey.value)
            }
        }
    }
}
