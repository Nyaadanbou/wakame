package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.getByteOrNull
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

/**
 * 物品的稀有度。不是所有物品都有稀有度，因此可能为空。
 */
@JvmInline
value class BRarityMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Rarity> {
    override val key: Key
        get() = ItemMetaConstants.createKey { RARITY }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.RARITY, ShadowTagType.BYTE) ?: false

    override fun getOrNull(): Rarity? {
        return accessor.rootOrNull
            ?.getByteOrNull(key.value())
            ?.let { RarityRegistry.findBy(it) }
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun set(value: Rarity) {
        accessor.rootOrCreate.putByte(key.value(), value.binaryId)
    }
}

internal fun BRarityMeta?.orDefault(): Rarity {
    return this?.getOrNull() ?: RarityRegistry.DEFAULT
}