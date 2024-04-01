package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.registry.ItemSkinRegistry
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.getShortOrNull
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的皮肤。
 */
internal class BSkinMeta(
    private val holder: ItemMetaAccessorImpl,
) : BinaryItemMeta<ItemSkin> {
    override val key: Key
        get() = ItemMetaKeys.SKIN
    override val exists: Boolean
        get() = holder.rootOrNull?.contains(ItemMetaKeys.SKIN.value(), ShadowTagType.SHORT) ?: false

    override fun getOrNull(): ItemSkin? {
        return holder.rootOrNull
            ?.getShortOrNull(key.value())
            ?.let { ItemSkinRegistry.findBy(it) }
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }

    override fun set(value: ItemSkin) {
        holder.rootOrCreate.putShort(key.value(), value.binaryId)
    }

    companion object : ItemMetaCompanion {
        override fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.SKIN.value(), ShadowTagType.SHORT)
        }
    }
}