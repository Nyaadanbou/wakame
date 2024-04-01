package cc.mewcraft.wakame.item

import me.lucko.helper.shadows.nbt.ShadowTag

/**
 * 代表一个可以转换成 NBT 的数据类。
 */
interface ShadowTagLike {
    fun asShadowTag(): ShadowTag
}