package cc.mewcraft.wakame.item.binary.cell.curse

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.CompoundShadowTag
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toStableShort
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

/**
 * Represents an empty binary curse.
 */
data object EmptyBinaryCurse : BinaryCurse {
    override val key: Key = Key(NekoNamespaces.CURSE, "empty")
    override fun test(context: NekoStack): Boolean = true
    override fun asShadowTag(): ShadowTag = CompoundShadowTag.create()
}

/**
 * Checks the number of entities killed by the item.
 *
 * @property index the entity reference to check with
 * @property count the required number of entities to be killed
 */
data class EntityKillsCurse(
    private val index: EntityReference,
    private val count: Int,
) : BinaryCurse {

    companion object Constants {
        const val INDEX_TAG_NAME = "index"
        const val COUNT_TAG_NAME = "count"
    }

    override val key: Key = CurseKeys.ENTITY_KILLS

    /**
     * Returns `true` if the number of entities killed by the item is greater
     * than [count]. Nota that the entity types are specified by [index].
     */
    override fun test(context: NekoStack): Boolean {
        var sum = 0
        for (k in index.keySet) {
            sum += context.statistics.ENTITY_KILLS[k]
        }
        return sum >= count
    }

    override fun asShadowTag(): ShadowTag {
        return CompoundShadowTag {
            putString(NekoTags.Cell.CURSE_KEY, key.asString())
            putString(INDEX_TAG_NAME, index.name)
            putShort(COUNT_TAG_NAME, count.toStableShort())
        }
    }
}

/**
 * Checks the highest damage dealt by the item.
 *
 * @property element the required source of damage
 * @property amount the required amount of damage
 */
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
    override fun test(context: NekoStack): Boolean =
        context.statistics.PEAK_DAMAGE[element] >= amount

    override fun asShadowTag(): ShadowTag {
        return CompoundShadowTag {
            putString(NekoTags.Cell.CURSE_KEY, key.asString())
            putShort(AMOUNT_TAG_NAME, amount.toStableShort())
            putByte(ELEMENT_TAG_NAME, element.binaryId)
        }
    }
}