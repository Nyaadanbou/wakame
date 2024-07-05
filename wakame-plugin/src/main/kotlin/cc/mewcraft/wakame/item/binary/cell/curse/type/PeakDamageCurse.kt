package cc.mewcraft.wakame.item.binary.cell.curse.type

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryPeakDamageCurse.Constants.AMOUNT_TAG_KEY
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryPeakDamageCurse.Constants.ELEMENT_TAG_KEY
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.key.Key

/**
 * Checks the highest damage dealt by the item.
 *
 * @property element the required source of damage
 * @property amount the required amount of damage
 */
interface BinaryPeakDamageCurse : BinaryCurse {
    val element: Element
    val amount: Int

    companion object Constants {
        const val AMOUNT_TAG_KEY = "amount"
        const val ELEMENT_TAG_KEY = "element"
    }

    override val key: Key
        get() = CurseConstants.createKey { PEAK_DAMAGE }

    /**
     * Returns `true` if the highest damage from [element] dealt by the item is
     * greater than [amount].
     */
    override fun test(context: NekoStack): Boolean {
        return context.statistics.PEAK_DAMAGE[element] >= amount
    }
}

fun BinaryPeakDamageCurse(
    compound: CompoundTag,
): BinaryPeakDamageCurse {
    return BinaryPeakDamageCurseNBTWrapper(compound)
}

fun BinaryPeakDamageCurse(
    element: Element,
    amount: Int,
): BinaryPeakDamageCurse {
    return BinaryPeakDamageCurseDataHolder(element, amount)
}

//
// Internal Implementations
//

internal data class BinaryPeakDamageCurseDataHolder(
    override val element: Element,
    override val amount: Int,
) : BinaryPeakDamageCurse {
    override fun serializeAsTag(): Tag = CompoundTag {
        putString(CurseBinaryKeys.CURSE_IDENTIFIER, key.asString())
        putShort(AMOUNT_TAG_KEY, amount.toStableShort())
        putByte(ELEMENT_TAG_KEY, element.binaryId)
    }
}

internal class BinaryPeakDamageCurseNBTWrapper(
    private val compound: CompoundTag,
) : BinaryPeakDamageCurse {
    override val element: Element
        get() = compound.getElement(ELEMENT_TAG_KEY)
    override val amount: Int
        get() = compound.getInt(AMOUNT_TAG_KEY)

    override fun clear() {
        compound.tags().clear()
    }

    override fun serializeAsTag(): Tag {
        return compound
    }

    override fun toString(): String {
        return compound.asString()
    }
}

private fun CompoundTag.getElement(key: String): Element {
    return ElementRegistry.getBy(this.getByte(key))
}