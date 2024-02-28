package cc.mewcraft.wakame.item.binary

import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 提供函数来修改一个 NekoItemStack 的状态。改动将直接应用到底层物品上。
 */
interface NekoItemStackSetter {
    /**
     * Erases all the custom tags from `this`.
     *
     * **Only to be used in certain special cases**.
     */
    fun erase()

    /**
     * Sets tags, overwriting any that are in `this`.
     */
    fun putRoot(compoundTag: CompoundShadowTag)

    /**
     * Sets seed.
     */
    fun putSeed(seed: Long)

    /**
     * Sets key.
     */
    fun putKey(key: Key)

    /**
     * Sets namespace.
     */
    fun putNamespace(namespace: String)

    /**
     * Sets ID.
     */
    fun putId(id: String)
}