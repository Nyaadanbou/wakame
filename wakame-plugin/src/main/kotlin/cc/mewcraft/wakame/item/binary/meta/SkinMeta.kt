package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.registry.ItemSkinRegistry
import cc.mewcraft.wakame.skin.ItemSkin
import cc.mewcraft.wakame.util.getShortOrNull
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

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
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.SKIN, TagType.SHORT) ?: false

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

    override fun provideDisplayLore(): LoreLine {
        val skin = get()
        val key = ItemMetaSupport.getLineKey(this) ?: return LoreLine.noop()
        val lines = ItemMetaSupport.mini().deserialize(tooltips.single, Placeholder.component("value", skin.displayName))
        return LoreLine.simple(key, listOf(lines))
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.SKIN
    ) {
        val tooltips: SingleTooltips = SingleTooltips()
    }
}