package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.util.getStringOrNull
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

/**
 * 物品的名字(MiniMessage).
 */
@JvmInline
value class BDisplayNameMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<String> {
    override val key: Key
        get() = ItemMetaConstants.createKey { DISPLAY_NAME }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.DISPLAY_NAME, ShadowTagType.STRING) ?: false

    override fun getOrNull(): String? {
        return accessor.rootOrNull?.getStringOrNull(key.value())
    }

    override fun set(value: String) {
        accessor.rootOrCreate.putString(key.value(), value)
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }
}

internal fun BDisplayNameMeta?.getOrEmpty(): String {
    return this?.getOrNull() ?: ""
}