package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.util.getIntOrNull
import cc.mewcraft.wakame.util.toStableByte
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的等级。不是所有物品都有等级，因此可能为空。
 */
internal class LevelMeta(
    private val holder: ItemMetaHolderImpl,
) : BinaryItemMeta<Int> {
    override val key: Key = ItemMetaKeys.LEVEL
    override val companion: ItemMetaCompanion = Companion

    override fun getOrNull(): Int? {
        return holder.rootOrNull?.getIntOrNull(key.value())
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }

    override fun set(value: Int) {
        holder.rootOrCreate.putByte(key.value(), value.toStableByte())
    }

    companion object : ItemMetaCompanion {
        override fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.LEVEL.value(), ShadowTagType.BYTE)
        }
    }
}

internal fun LevelMeta?.orDefault(): Int {
    return this?.get() ?: 1
}