package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.util.getStringOrNull
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的名字(MiniMessage).
 */
class BDisplayNameMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<String> {
    override val key: Key
        get() = ItemMetaKeys.DISPLAY_NAME
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaKeys.DISPLAY_NAME.value(), ShadowTagType.STRING) ?: false

    override fun getOrNull(): String? {
        return accessor.rootOrNull?.getStringOrNull(key.value())
    }

    override fun set(value: String) {
        accessor.rootOrCreate.putString(key.value(), value)
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    companion object : ItemMetaCompanion {
        override operator fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.DISPLAY_NAME.value(), ShadowTagType.STRING)
        }
    }
}

internal fun BDisplayNameMeta?.getOrEmpty(): String {
    return this?.getOrNull() ?: ""
}