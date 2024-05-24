package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NoopLoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.getByteOrNull
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

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

    override fun provideDisplayLore(): LoreLine {
        val rarity = get()
        val key = ItemMetaSupport.getLineKey(this) ?: return NoopLoreLine
        val lines = ItemMetaSupport.mini().deserialize(tooltips.single, Placeholder.component("value", rarity.displayName))
        return ItemMetaLoreLine(key, listOf(lines))
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.RARITY
    ) {
        val tooltips: SingleTooltips = SingleTooltips()
    }
}

fun BRarityMeta?.getOrDefault(): Rarity {
    return this?.getOrNull() ?: RarityRegistry.DEFAULT
}