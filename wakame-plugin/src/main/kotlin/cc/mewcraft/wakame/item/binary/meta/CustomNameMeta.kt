package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.ReloadableProperty
import cc.mewcraft.wakame.display.DisplayNameProvider
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NoopLoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.getStringOrNull
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * 物品的自定义名字(MiniMessage).
 */
@JvmInline
value class BCustomNameMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<String>, DisplayNameProvider {
    override val key: Key
        get() = ItemMetaConstants.createKey { CUSTOM_NAME }

    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.CUSTOM_NAME, ShadowTagType.STRING) ?: false

    override fun getOrNull(): String? {
        return accessor.rootOrNull?.getStringOrNull(key.value())
    }

    override fun set(value: String) {
        accessor.rootOrCreate.putString(key.value(), value)
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun provideDisplayLore(): LoreLine {
        return NoopLoreLine // The name item meta never has lore line
    }

    override fun provideDisplayName(): Component {
        val customName = getOrNull() ?: return DisplayNameSupport.EMPTY

        val resolvers = TagResolver.builder().apply {

            // create <value> tag
            resolver(Placeholder.parsed("value", customName))

            // create <rarity_name> & <rarity_style> tags
            val rarityOrDefault = accessor.item.getMetaAccessor<BRarityMeta>().getOrDefault()
            resolvers(DisplayNameSupport.CACHE[rarityOrDefault])
        }

        return ItemMetaSupport.mini().deserialize(tooltips.single, resolvers.build())
    }

    private companion object : ItemMetaConfig(
        ItemMetaConstants.CUSTOM_NAME
    ) {
        val tooltips: SingleTooltips = SingleTooltips()
    }
}

fun BCustomNameMeta?.getOrEmpty(): String {
    return this?.getOrNull() ?: ""
}

private object DisplayNameSupport {
    val EMPTY: Component = text("Unnamed")
    val CACHE: LoadingCache<Rarity, TagResolver> by ReloadableProperty {
        Caffeine.newBuilder().build { rarity ->
            TagResolver.resolver(
                Placeholder.component("rarity_name", rarity.displayName),
                Placeholder.styling("rarity_style", *rarity.styles)
            )
        }
    }
}
