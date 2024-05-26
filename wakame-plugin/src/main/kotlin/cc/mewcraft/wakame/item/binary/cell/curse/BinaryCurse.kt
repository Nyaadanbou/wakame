package cc.mewcraft.wakame.item.binary.cell.curse

import cc.mewcraft.wakame.condition.Condition
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.TagLike
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEmptyCurse

/**
 * Represents a [Curse] in binary form.
 */
interface BinaryCurse : Curse, TagLike, Condition<NekoStack<*>> {

    /**
     * Checks whether the curse is unlocked or not.
     *
     * @param context the item by which the curse is owned
     * @return `true` if the curse is unlocked, else wise `false`
     */
    override fun test(context: NekoStack<*>): Boolean

    /**
     * Clears the curse so that it becomes empty.
     */
    fun clear() = Unit
}

/**
 * Checks if the curse is empty.
 */
val BinaryCurse.isEmpty: Boolean get() = (this is BinaryEmptyCurse)
