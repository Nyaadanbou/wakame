package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.getByteOrNull
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key

/**
 * 物品的稀有度。不是所有物品都有稀有度，因此可能为空。
 */
internal class RarityMeta(
    private val holder: ItemMetaHolderImpl,
) : BinaryItemMeta<Rarity> {
    override val key: Key = ItemMetaKeys.RARITY
    override val companion: ItemMetaCompanion = Companion

    override fun getOrNull(): Rarity? {
        return holder.rootOrNull
            ?.getByteOrNull(key.value())
            ?.let { RarityRegistry.findBy(it) }
    }

    override fun remove() {
        holder.rootOrNull?.remove(key.value())
    }

    override fun set(value: Rarity) {
        holder.rootOrCreate.putByte(key.value(), value.binaryId)
    }

    companion object : ItemMetaCompanion {
        override fun contains(compound: CompoundShadowTag): Boolean {
            return compound.contains(ItemMetaKeys.RARITY.value(), ShadowTagType.BYTE)
        }
    }
}

internal fun RarityMeta?.orDefault(): Rarity {
    return this?.getOrNull() ?: RarityRegistry.DEFAULT
}