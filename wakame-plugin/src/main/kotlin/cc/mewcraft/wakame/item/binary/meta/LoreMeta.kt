package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.nbt.ListTag
import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.util.getListOrNull
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/**
 * 物品的描述(MiniMessage).
 */
@JvmInline
value class BLoreMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<List<String>> {
    override val key: Key
        get() = ItemMetaConstants.createKey { LORE }

    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.LORE, TagType.LIST) ?: false

    override fun getOrNull(): List<String>? {
        return accessor.rootOrNull
            ?.getListOrNull(key.value(), TagType.STRING)
            ?.map { (it as StringTag).value() }
    }

    override fun set(value: List<String>) {
        val stringTags = value.map(StringTag::valueOf)
        val listShadowTag = ListTag.create(stringTags, TagType.STRING)
        accessor.rootOrCreate.put(key.value(), listShadowTag)
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun provideDisplayLore(): LoreLine {
        val lore = get()
        val key = ItemMetaSupport.getLineKey(this) ?: return LoreLine.noop()
        val lines = lore.mapTo(ObjectArrayList(lore.size)) { ItemMetaSupport.mini().deserialize(tooltips.line, Placeholder.parsed("line", it)) }

        val header = tooltips.header?.map(ItemMetaSupport.mini()::deserialize).orEmpty()
        val bottom = tooltips.bottom?.map(ItemMetaSupport.mini()::deserialize).orEmpty()
        lines.addAll(0, header)
        lines.addAll(bottom)

        return LoreLine.simple(key, lines)
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.LORE
    ) {
        val tooltips: LoreTooltips = LoreTooltips()
    }
}

fun BLoreMeta?.getOrEmpty(): List<String> {
    return this?.getOrNull() ?: emptyList()
}