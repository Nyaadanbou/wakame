package cc.mewcraft.wakame.item.binary

import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 提供函数来修改一个 WakaItemStack 的状态。改动将直接应用到底层物品上。
 */
interface WakaItemStackSetter {
    /**
     * Sets tags, overwriting any that are in `this`.
     */
    fun putRoot(compoundTag: CompoundShadowTag)

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
    fun putPath(id: String)
}