package cc.mewcraft.wakame.item.binary.cell.curse

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.ShadowTagLike
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEmptyCurse

/**
 * Represents a [Curse] in binary form.
 */
interface BinaryCurse : Curse, ShadowTagLike, Condition<NekoStack>

/**
 * Checks if the curse is empty.
 */
val BinaryCurse.isEmpty: Boolean get() = (this is BinaryEmptyCurse)
