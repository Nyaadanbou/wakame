package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.BinaryData
import cc.mewcraft.wakame.item.CurseKeys
import cc.mewcraft.wakame.reference.EntityReference
import cc.mewcraft.wakame.util.compoundShadowTag
import cc.mewcraft.wakame.util.toStableShort
import me.lucko.helper.shadows.nbt.ShadowTag
import net.kyori.adventure.key.Key

/**
 * Checks the number of entities killed by the item.
 *
 * @property index the entity reference to check with
 * @property count the required number of entities to be killed
 */
@BinaryData
class EntityKillsCurse(
    private val index: EntityReference,
    private val count: Int,
) : BinaryCurse, Initializable {

    companion object Constants {
        const val ID_TAG_NAME = "id"
        const val INDEX_TAG_NAME = "index"
        const val COUNT_TAG_NAME = "count"
    }

    override val key: Key = CurseKeys.ENTITY_KILLS

    /**
     * Returns `true` if the number of entities killed by the item is greater
     * than [count]. Nota that the entity types are specified by [index].
     */
    override fun test(context: BinaryCurseContext): Boolean {
        var sum = 0
        for (k in index.keySet) {
            sum += context.statsContext.entityKillsStats.get(k)
        }
        return sum >= count
    }

    override fun asShadowTag(): ShadowTag {
        return compoundShadowTag {
            putString(ID_TAG_NAME, key.asString())
            putString(INDEX_TAG_NAME, index.name)
            putShort(COUNT_TAG_NAME, count.toStableShort())
        }
    }

    override fun onPreWorld() {
        // TODO load it to the factory
    }
}