package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.wakame.display.TagResolverProvider
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.TagLike
import cc.mewcraft.wakame.item.binary.cell.core.empty.BinaryEmptyCore
import cc.mewcraft.wakame.item.binary.cell.core.noop.BinaryNoopCore

//
// BinaryCore 分为两类
//
// 1. 一个 NBT 对象的封装类（NBTWrapper）
// 2. 一个直接持有数据的数据类（DataHolder）
//
// NBTWrapper 用于读取 NBT 数据，也作为其他系统读取 NBT 数据的抽象层。
// 正如其名字一样——封装类，构建一个 NBTWrapper 首先需要一个 NBT 对象。
//
// DataHolder 自己直接持有数据的值。
// 这也意味着构建一个 DataHolder 需要首先已知所有的数据值，
// 因此它都是由 Config 或者 SchemaCore 创建而来。

/**
 * Represents a **binary** [Core]. The name "binary" implies the
 * [core][Core] being a data representation that could be directly
 * used in the world state.
 */
interface BinaryCore : Core, TagResolverProvider, TagLike {
    /**
     * Clears the core so that it becomes empty.
     *
     * The actual behavior of this function is implementation-defined!
     */
    fun clear() = Unit
}

/**
 * Checks if the core is noop.
 */
val BinaryCore.isNoop: Boolean get() = (this is BinaryNoopCore)

/**
 * Checks if the core is empty.
 */
val BinaryCore.isEmpty: Boolean get() = (this is BinaryEmptyCore)
