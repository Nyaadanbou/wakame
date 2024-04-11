package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.registry.ItemSkinRegistry
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.getShortOrNull
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

/**
 * 物品的皮肤。
 */
@JvmInline
value class BSkinMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<ItemSkin> {
    override val key: Key
        get() = ItemMetaConstants.createKey { SKIN }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.SKIN, ShadowTagType.SHORT) ?: false

    override fun getOrNull(): ItemSkin? {
        return accessor.rootOrNull
            ?.getShortOrNull(key.value())
            ?.let { ItemSkinRegistry.findBy(it) }
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun set(value: ItemSkin) {
        accessor.rootOrCreate.putShort(key.value(), value.binaryId)
    }
}