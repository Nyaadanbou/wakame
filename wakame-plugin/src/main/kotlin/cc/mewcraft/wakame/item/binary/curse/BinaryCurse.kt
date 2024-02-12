package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.ShadowTagLike
import cc.mewcraft.wakame.item.scheme.curse.SchemeCurse
import cc.mewcraft.wakame.util.NumericValue

/**
 * Represents a curse in binary form.
 *
 * The name "binary" is used to distinguish from [SchemeCurse] which stores
 * numeric values as [NumericValue]. In contrast, [BinaryCurse] simply
 * stores numeric values as primitives since it's a reflection on NBT data.
 */
sealed interface BinaryCurse : Curse, ShadowTagLike, Condition<BinaryCurseContext>

/**
 * Gets the empty condition.
 */
@OptIn(InternalApi::class)
fun emptyBinaryCurse(): BinaryCurse = EmptyBinaryCurse

@OptIn(InternalApi::class)
val BinaryCurse.isEmpty: Boolean get() = this is EmptyBinaryCurse
val BinaryCurse.isNotEmpty: Boolean get() = !isEmpty