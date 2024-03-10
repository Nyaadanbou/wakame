package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.BinaryData
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.util.compoundShadowTag
import cc.mewcraft.wakame.util.toStableShort
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

/**
 * Checks the highest damage dealt by the item.
 *
 * @property element the required source of damage
 * @property amount the required amount of damage
 */
@BinaryData
data class PeakDamageCurse(
    private val element: Element,
    private val amount: Int,
) : BinaryCurse {

    companion object Constants {
        const val AMOUNT_TAG_NAME = "amount"
        const val ELEMENT_TAG_NAME = "elem"
    }

    override val key: Key = CurseKeys.PEAK_DAMAGE

    /**
     * Returns `true` if the highest damage from [element] dealt by the item is
     * greater than [amount].
     */
    override fun test(context: NekoItemStack): Boolean =
        context.statistics.peakDamage.get(element) >= amount

    override fun asShadowTag(): ShadowTag {
        return compoundShadowTag {
            putString(NekoTags.Cell.CURSE_KEY, key.asString())
            putShort(AMOUNT_TAG_NAME, amount.toStableShort())
            putByte(ELEMENT_TAG_NAME, element.binary)
        }
    }
}