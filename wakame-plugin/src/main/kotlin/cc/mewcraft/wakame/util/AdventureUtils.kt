package cc.mewcraft.wakame.util

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag

fun compoundBinaryTag(builder: CompoundBinaryTag.Builder.() -> Unit): CompoundBinaryTag =
    CompoundBinaryTag.builder().apply(builder).build()

fun listBinaryTag(builder: ListBinaryTag.Builder<BinaryTag>.() -> Unit): ListBinaryTag =
    ListBinaryTag.builder().apply(builder).build()

fun listBinaryTag(vararg tags: BinaryTag): ListBinaryTag =
    listBinaryTag { tags.forEach(this::add) }