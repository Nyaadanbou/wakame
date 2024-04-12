package cc.mewcraft.wakame.item.binary

import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 提供函数来修改一个 NekoStack 的状态。改动将直接应用到底层物品上。
 */
interface NekoStackSetter {
    /**
     * Removes all the custom tags from the item.
     *
     * **Only to be used in certain special cases!**
     */
    fun erase()

    /**
     * Sets the tags, overwriting any that are in the item.
     */
    fun putRoot(compoundTag: CompoundShadowTag)

    /**
     * Sets the key.
     *
     * It's equivalent to call both [putNamespace] and [putPath] at
     * the same time under the hood.
     */
    fun putKey(key: Key)

    /**
     * Sets the namespace.
     */
    fun putNamespace(namespace: String)

    /**
     * Sets the path.
     */
    fun putPath(path: String)

    /**
     * Sets the variant.
     */
    fun putVariant(sid: Int)
}