package cc.mewcraft.wakame.item.components.cells.curses

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NameLine
import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.CurseConstants
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.CurseConfig
import cc.mewcraft.wakame.item.components.cells.CurseType
import cc.mewcraft.wakame.item.components.tracks.TrackTypes
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
    override val key: Key
        get() = Type.key
    override val type: CurseType<CurseEntityKills> = Type
    override val isEmpty: Boolean = false

    override fun isLocked(context: NekoStack): Boolean {
        return !isUnlocked(context)
    }

    override fun isUnlocked(context: NekoStack): Boolean {
        val tracks = context.components.get(ItemComponentTypes.TRACKS) ?: return false
        val track = tracks.get(TrackTypes.ENTITY_KILLS) ?: return false // 如果没有统计数据, 则返回锁定状态
        var sum = 0
        for (key in index.keySet) {
            sum += track.get(key)
        }
        return sum >= count
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putString(CurseBinaryKeys.CURSE_IDENTIFIER, key.asString())
        putString(TAG_INDEX, index.name)
        putShort(TAG_COUNT, count.toStableShort())
    }

    override fun provideTooltipName(): NameLine {
        return NameLine.simple(config.displayName)
    }

    override fun provideTooltipLore(): LoreLine {
        return LoreLine.simple(key, listOf(tooltip.render()))
    }

    companion object Type : CurseType<CurseEntityKills> {
        const val TAG_INDEX = "index"
        const val TAG_COUNT = "count"
        val key = CurseConstants.createKey { ENTITY_KILLS }

        private val config: CurseConfig = CurseConfig(CurseConstants.ENTITY_KILLS)
        private val tooltip: CurseConfig.SingleTooltip = config.SingleTooltip()
    }
}

private fun CompoundTag.getEntityReference(key: String): EntityTypeHolder {
    return EntityRegistry.TYPES[this.getString(key)]
}
