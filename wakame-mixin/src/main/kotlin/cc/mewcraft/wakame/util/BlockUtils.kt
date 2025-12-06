package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.BlockUtils.Companion.INSTANCE
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.tag.TagKey
import org.bukkit.block.Block
import org.bukkit.block.BlockType
import org.jetbrains.annotations.ApiStatus

/**
 * 各种和方块相关的方便函数.
 * 添加自定义方块相关插件后, 各函数的实现可能会变化.
 */
interface BlockUtils {
    fun isCustomBlock(block: Block): Boolean

    fun getBlockId(block: Block): Identifier

    fun isTagged(block: Block, tagKey: TagKey<BlockType>): Boolean

    /**
     * 伴生对象, 提供 [BlockUtils] 的实例.
     */
    companion object {

        @get:JvmName("getInstance")
        var INSTANCE: BlockUtils = Default
            private set

        @ApiStatus.Internal
        fun register(instance: BlockUtils) {
            this.INSTANCE = instance
        }

    }
}

/**
 * [BlockUtils] 的默认实现.
 */
private object Default : BlockUtils {
    override fun isCustomBlock(block: Block): Boolean {
        return false
    }

    override fun getBlockId(block: Block): Identifier {
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

// 方便外部使用的拓展函数
fun Block.isCustomBlock(): Boolean {
    return INSTANCE.isCustomBlock(this)
}

fun Block.getBlockId(): Identifier{
    return INSTANCE.getBlockId(this)
}

fun Block.isTagged(tagKey: TagKey<BlockType>): Boolean {
    return INSTANCE.isTagged(this, tagKey)
}

