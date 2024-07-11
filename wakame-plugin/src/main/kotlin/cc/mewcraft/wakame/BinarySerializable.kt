package cc.mewcraft.wakame

import cc.mewcraft.nbt.Tag

/**
 * 代表一个可以转换成 NBT 的对象。
 */
interface BinarySerializable {
    fun serializeAsTag(): Tag
}