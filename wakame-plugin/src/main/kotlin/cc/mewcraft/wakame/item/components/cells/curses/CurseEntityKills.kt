package cc.mewcraft.wakame.item.components.cells.curses

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.CurseType
import cc.mewcraft.wakame.registry.EntityRegistry
import cc.mewcraft.wakame.util.CompoundTag
import cc.mewcraft.wakame.util.toStableShort
import net.kyori.adventure.key.Key

/**
 * 从 NBT 创建一个 [CurseEntityKills].
 */
fun CurseEntityKills(nbt: CompoundTag): CurseEntityKills {
    val index = nbt.getEntityReference(CurseEntityKills.TAG_INDEX)
    val count = nbt.getInt(CurseEntityKills.TAG_COUNT)
    return CurseEntityKills(index, count)
}

/**
 * Checks the number of entities killed by the item.
 *
 * @property index the entity reference to check with
 * @property count the required number of entities to be killed
 */
data class CurseEntityKills(
    val index: EntityTypeHolder,
    val count: Int,
) : Curse {
    override val key: Key = CurseConstants.createKey { ENTITY_KILLS }
    override val type: CurseType<CurseEntityKills> = Type
    override val isEmpty: Boolean = false

    override fun isLocked(context: NekoStack): Boolean {
        return !isUnlocked(context)
    }

    override fun isUnlocked(context: NekoStack): Boolean {
        var sum = 0
        for (k in index.keySet) {
            // TODO 完成组件 ItemTracks
            // sum += context.statistics.ENTITY_KILLS[k]
            sum += 1
        }
        return sum >= count
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putString(CurseBinaryKeys.CURSE_IDENTIFIER, key.asString())
        putString(TAG_INDEX, index.name)
        putShort(TAG_COUNT, count.toStableShort())
    }

    internal companion object Type : CurseType<CurseEntityKills> {
        const val TAG_INDEX = "index"
        const val TAG_COUNT = "count"
    }
}

private fun CompoundTag.getEntityReference(key: String): EntityTypeHolder {
    return EntityRegistry.TYPES[this.getString(key)]
}
