package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.util.getStringOrNull
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的名字(MiniMessage).
 */
internal class DisplayNameMeta(
    private val holder: ItemMetaHolderImpl,
) : ItemMeta<String> {
    override val key: Key = ItemMetaKeys.DISPLAY_NAME
    override val companion: ItemMetaCompanion = Companion

    override fun getOrNull(): String? {
        return holder.rootOrNull?.getStringOrNull(key.value())
    }

    override fun set(value: String) {
        holder.rootOrCreate.putString(key.value(), value)
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }

    companion object : ItemMetaCompanion {
        override operator fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.DISPLAY_NAME.value(), ShadowTagType.STRING)
        }
    }
}

internal fun DisplayNameMeta?.orEmpty(): String {
    return this?.get() ?: ""
}