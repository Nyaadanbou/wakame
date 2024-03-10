package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.ShadowTagLike
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.scheme.curse.SchemeCurse
import cc.mewcraft.wakame.util.RandomizedValue

/**
 * Represents a curse in binary form.
 *
 * The name "binary" is used to distinguish from [SchemeCurse] which stores
 * numeric values as [RandomizedValue]. In contrast, [BinaryCurse] simply
 * stores numeric values as primitives since it's a reflection on NBT data.
 */
sealed interface BinaryCurse : Curse, ShadowTagLike, Condition<NekoItemStack>

/**
 * Gets the empty condition.
 */
fun emptyBinaryCurse(): BinaryCurse = @OptIn(InternalApi::class) EmptyBinaryCurse

/**
 * Checks if the curse is empty.
 */
val BinaryCurse.isEmpty: Boolean get() = @OptIn(InternalApi::class) (this is EmptyBinaryCurse)

/**
 * Checks if the curse is not empty.
 */
val BinaryCurse.isNotEmpty: Boolean get() = !isEmpty