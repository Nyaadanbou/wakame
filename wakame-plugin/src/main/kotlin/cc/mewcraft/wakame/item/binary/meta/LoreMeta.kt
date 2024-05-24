package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.util.getListOrNull
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.ListShadowTag
import me.lucko.helper.shadows.nbt.StringShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

/**
 * 物品的描述(MiniMessage).
 */
// TODO cell-overhaul: 类名不需要以 "B" 开头
// TODO cell-overhaul: 类的选择应该有个单例持有所有类型，类似原版的 DataComponent，继而弃用 reified class
@JvmInline
value class BLoreMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<List<String>> {
    override val key: Key
        get() = ItemMetaConstants.createKey { LORE }

    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.LORE, ShadowTagType.LIST) ?: false

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

    override fun provideDisplayLore(): LoreLine {
        val lore = get()
        val key = ItemMetaSupport.getLineKey(this)
        val lines = lore.mapTo(ObjectArrayList(lore.size)) { ItemMetaSupport.mini().deserialize(tooltips.line, Placeholder.parsed("line", it)) }

        val header = tooltips.header?.map(ItemMetaSupport.mini()::deserialize).orEmpty()
        val bottom = tooltips.bottom?.map(ItemMetaSupport.mini()::deserialize).orEmpty()
        lines.addAll(0, header)
        lines.addAll(bottom)

        return ItemMetaLoreLine(key, lines)
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