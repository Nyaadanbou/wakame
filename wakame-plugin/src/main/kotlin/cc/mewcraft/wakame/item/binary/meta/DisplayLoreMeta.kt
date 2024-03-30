package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.util.getListOrNull
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ListShadowTag
import me.lucko.helper.shadows.nbt.StringShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的描述(MiniMessage).
 */
internal class DisplayLoreMeta(
    private val holder: ItemMetaHolderImpl,
) : BinaryItemMeta<List<String>> {
    override val key: Key = ItemMetaKeys.DISPLAY_LORE

    override fun getOrNull(): List<String>? {
        return holder.rootOrNull
            ?.getListOrNull(key.value(), ShadowTagType.STRING)
            ?.map { (it as StringShadowTag).value() }
    }

    override fun set(value: List<String>) {
        val stringTags = value.map(StringShadowTag::valueOf)
        val listShadowTag = ListShadowTag.create(stringTags, ShadowTagType.STRING)
        holder.rootOrCreate.put(key.value(), listShadowTag)
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }



    companion object : ItemMetaCompanion {
        override operator fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.DISPLAY_LORE.value(), ShadowTagType.LIST)
        }
    }
}

internal fun DisplayLoreMeta?.orEmpty(): List<String> {
    return this?.getOrNull() ?: emptyList()
}