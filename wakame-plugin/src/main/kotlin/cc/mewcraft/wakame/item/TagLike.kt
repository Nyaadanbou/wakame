package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.Tag

/**
 * 代表一个可以转换成 NBT 的数据类。
 */
interface TagLike {
    fun asTag(): Tag
}