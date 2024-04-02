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
@JvmInline
value class BLevelMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Int> {
    override val key: Key
        get() = ItemMetaKeys.LEVEL
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaKeys.LEVEL.value(), ShadowTagType.BYTE) ?: false

    override fun getOrNull(): Int? {
        return accessor.rootOrNull?.getIntOrNull(key.value())
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun set(value: Int) {
        require(value >= 0) { "level >= 0" }
        accessor.rootOrCreate.putByte(key.value(), value.toStableByte())
    }

    companion object : ItemMetaCompanion {
        override fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.LEVEL.value(), ShadowTagType.BYTE)
        }
    }
}

internal fun BLevelMeta?.orDefault(): Int {
    return this?.getOrNull() ?: 0
}