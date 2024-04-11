package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.util.getIntOrNull
import cc.mewcraft.wakame.util.toStableByte
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

/**
 * 物品的等级。不是所有物品都有等级，因此可能为空。
 */
@JvmInline
value class BLevelMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Int> {
    override val key: Key
        get() = ItemMetaConstants.createKey { LEVEL }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.LEVEL, ShadowTagType.BYTE) ?: false

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
}

internal fun BLevelMeta?.orDefault(): Int {
    return this?.getOrNull() ?: 0
}