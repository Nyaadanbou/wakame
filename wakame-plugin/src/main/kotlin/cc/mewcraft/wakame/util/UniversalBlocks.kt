package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.LOGGER
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.block.Block
import org.bukkit.block.BlockType

/**
 * 各种和方块相关的方便函数.
 * 添加自定义方块相关插件后, 各函数的实现可能会变化.
 */
interface UniversalBlocks {

    fun isCustomBlock(block: Block): Boolean

    fun getBlockId(block: Block): KoishKey

    fun isTagged(block: Block, tagKey: TagKey<BlockType>): Boolean

    /**
     * 伴生对象, 提供 [UniversalBlocks] 的实现.
     */
    companion object : UniversalBlocks {

        private var implementation: UniversalBlocks = object : UniversalBlocks {
            override fun isCustomBlock(block: Block): Boolean {
                return false
            }

            override fun getBlockId(block: Block): KoishKey {
                return block.type.key()
            }

            override fun isTagged(block: Block, tagKey: TagKey<BlockType>): Boolean {
                val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK)
                if (!registry.hasTag(tagKey)) {
                    LOGGER.warn("Block tag not found: '${tagKey.key()}'")
                    return false
                } else {
                    return registry.getTag(tagKey).contains(TypedKey.create(RegistryKey.BLOCK, block.type.key()))
                }
            }
        }

        fun setImplementation(impl: UniversalBlocks) {
            implementation = impl
        }

        override fun isCustomBlock(block: Block): Boolean {
            return implementation.isCustomBlock(block)
        }

        override fun getBlockId(block: Block): KoishKey {
            return implementation.getBlockId(block)
        }

        override fun isTagged(block: Block, tagKey: TagKey<BlockType>): Boolean {
            return implementation.isTagged(block, tagKey)
        }
    }
}
