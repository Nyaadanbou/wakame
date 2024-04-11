package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.util.getListOrNull
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.ListShadowTag
import me.lucko.helper.shadows.nbt.StringShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的描述(MiniMessage).
 */
@JvmInline
value class BDisplayLoreMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<List<String>> {
    override val key: Key
        get() = ItemMetaConstants.createKey { DISPLAY_LORE }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.DISPLAY_LORE, ShadowTagType.LIST) ?: false

    override fun getOrNull(): List<String>? {
        return accessor.rootOrNull
            ?.getListOrNull(key.value(), ShadowTagType.STRING)
            ?.map { (it as StringShadowTag).value() }
    }

    override fun set(value: List<String>) {
        val stringTags = value.map(StringShadowTag::valueOf)
        val listShadowTag = ListShadowTag.create(stringTags, ShadowTagType.STRING)
        accessor.rootOrCreate.put(key.value(), listShadowTag)
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }
}

internal fun BDisplayLoreMeta?.getOrEmpty(): List<String> {
    return this?.getOrNull() ?: emptyList()
}