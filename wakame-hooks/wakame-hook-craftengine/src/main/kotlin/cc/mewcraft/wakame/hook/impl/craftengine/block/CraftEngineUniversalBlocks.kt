package cc.mewcraft.wakame.hook.impl.craftengine.block

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.hook.impl.craftengine.CKey
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.UniversalBlocks
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.tag.TagKey
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks
import org.bukkit.block.Block
import org.bukkit.block.BlockType

/**
 * 由 CraftEngine 提供的 [cc.mewcraft.wakame.util.UniversalBlocks] 实现.
 */
object CraftEngineUniversalBlocks : UniversalBlocks {

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

    override fun isTagged(block: Block, tagKey: TagKey<BlockType>): Boolean {
        val immutableBlockState = CraftEngineBlocks.getCustomBlockState(block)
        if (immutableBlockState != null) {
            val tagId = tagKey.key()
            return immutableBlockState.settings().tags().contains(CKey.of(tagId.namespace(), tagId.value()))
        }

        val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK)
        if (!registry.hasTag(tagKey)) {
            LOGGER.warn("Block tag not found: '${tagKey.key()}'")
            return false
        } else {
            return registry.getTag(tagKey).contains(TypedKey.create(RegistryKey.BLOCK, block.type.key()))
        }
    }
}