package cc.mewcraft.wakame.item

import net.kyori.adventure.nbt.BinaryTag

/**
 * Something can be serialized to a NBT tag.
 */
interface TagSerializable {
    fun save(): BinaryTag
    fun load(tag: BinaryTag)
}