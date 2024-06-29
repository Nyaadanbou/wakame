package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.display.NameLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.getStringOrNull
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 物品的名字。
 *
 * 注意区别于 [BCustomNameMeta]。
 */
@JvmInline
value class BItemNameMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<String> {
    override val key: Key
        get() = ItemMetaConstants.createKey { ITEM_NAME }

    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.ITEM_NAME, TagType.STRING) ?: false

    override fun getOrNull(): String? {
        return accessor.rootOrNull?.getStringOrNull(key.value())
    }

    override fun set(value: String) {
        accessor.rootOrCreate.putString(key.value(), value)
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun provideDisplayName(): NameLine {
        // 代码复制于 DisplayNameMeta

        val customName = getOrNull() ?: return ItemNameImplementations.EMPTY

        val resolvers = TagResolver.builder().apply {

            // create <value> tag
            resolver(Placeholder.parsed("value", customName))

            // create <rarity_name> & <rarity_style> tags
            val rarityOrDefault = accessor.item.getMetaAccessor<BRarityMeta>().getOrDefault()
            resolvers(ItemNameImplementations.CACHE[rarityOrDefault])
        }

        return NameLine.simple(ItemMetaSupport.mini().deserialize(tooltips.single, resolvers.build()))
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.ITEM_NAME
    ) {
        val tooltips: SingleTooltips = SingleTooltips()
    }
}

fun BItemNameMeta?.getOrEmpty(): String {
    return this?.getOrNull() ?: ""
}

private object ItemNameImplementations {
    val EMPTY: NameLine = NameLine.simple(text("Unnamed"))
    val CACHE: LoadingCache<Rarity, TagResolver> by ReloadableProperty {
        Caffeine.newBuilder().build { rarity ->
            TagResolver.resolver(
                Placeholder.component("rarity_name", rarity.displayName),
                Placeholder.styling("rarity_style", *rarity.styles)
            )
        }
    }
}
