package cc.mewcraft.wakame.item.binary.cell.curse.type

import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEntityKillsCurse.Constants.COUNT_TAG_KEY
import cc.mewcraft.wakame.item.binary.cell.curse.type.BinaryEntityKillsCurse.Constants.INDEX_TAG_KEY
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.util.CompoundShadowTag
import cc.mewcraft.wakame.util.toStableShort
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

/**
 * Checks the number of entities killed by the item.
 *
 * @property index the entity reference to check with
 * @property count the required number of entities to be killed
 */
interface BinaryEntityKillsCurse : BinaryCurse {
    val index: EntityTypeHolder
    val count: Int

    companion object Constants {
        const val INDEX_TAG_KEY = "index"
        const val COUNT_TAG_KEY = "count"
    }

    override val key: Key
        get() = CurseConstants.createKey { ENTITY_KILLS }

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
}

fun BinaryEntityKillsCurse(
    compound: CompoundShadowTag,
): BinaryEntityKillsCurse {
    return BinaryEntityKillsCurseNBTWrapper(compound)
}

fun BinaryEntityKillsCurse(
    index: EntityTypeHolder,
    count: Int,
): BinaryEntityKillsCurse {
    return BinaryEntityKillsCurseDataHolder(index, count)
}

//
// Internal Implementations
//

internal data class BinaryEntityKillsCurseDataHolder(
    override val index: EntityTypeHolder,
    override val count: Int,
) : BinaryEntityKillsCurse {
    override fun asShadowTag(): ShadowTag = CompoundShadowTag {
        putString(CurseBinaryKeys.CURSE_IDENTIFIER, key.asString())
        putString(INDEX_TAG_KEY, index.name)
        putShort(COUNT_TAG_KEY, count.toStableShort())
    }
}

internal class BinaryEntityKillsCurseNBTWrapper(
    private val compound: CompoundShadowTag,
) : BinaryEntityKillsCurse {
    override val index: EntityTypeHolder
        get() = compound.getEntityReference(INDEX_TAG_KEY)
    override val count: Int
        get() = compound.getInt(COUNT_TAG_KEY)

    override fun clear() {
        compound.tags().clear()
    }

    override fun asShadowTag(): ShadowTag = compound
    override fun toString(): String = compound.asString()
}

private fun CompoundShadowTag.getEntityReference(key: String): EntityTypeHolder {
    return EntityRegistry.TYPES[this.getString(key)]
}
