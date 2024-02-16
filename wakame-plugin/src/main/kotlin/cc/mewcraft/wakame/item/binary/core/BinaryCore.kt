package cc.mewcraft.wakame.item.binary.core

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.BinaryCoreValue
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.ShadowTagLike

/**
 * Represents a **binary** [Core]. The name "binary" implies the
 * [core][Core] being a representation in the game world. Currently, it
 * simply reflects the NBT structure in items.
 */
interface BinaryCore : Core, ShadowTagLike {
    val value: BinaryCoreValue
}

/**
 * Gets the empty core.
 */
fun emptyBinaryCore(): BinaryCore = @OptIn(InternalApi::class) EmptyBinaryCore

val BinaryCore.isEmpty: Boolean get() = @OptIn(InternalApi::class) (this is EmptyBinaryCore)
val BinaryCore.isNotEmpty: Boolean get() = !isEmpty
