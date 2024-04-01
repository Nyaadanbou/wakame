package cc.mewcraft.wakame.item.binary.cell.core

import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.ShadowTagLike

/**
 * Represents a **binary** [Core]. The name "binary" implies the
 * [core][Core] being a representation in the game world. Currently, it
 * simply reflects the NBT structure in items.
 */
interface BinaryCore : Core, ShadowTagLike

/**
 * Checks if the core is empty.
 */
val BinaryCore.isEmpty: Boolean get() = (this is EmptyBinaryCore)

/**
 * Checks if the core is not empty.
 */
val BinaryCore.isNotEmpty: Boolean get() = !isEmpty
