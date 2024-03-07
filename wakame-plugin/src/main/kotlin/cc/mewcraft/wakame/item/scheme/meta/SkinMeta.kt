package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的皮肤。
 */
sealed interface SkinMeta : SchemeItemMeta<ItemSkin> {
    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "skin")
    }
}

private class NonNullSkinMeta(
    private val itemSkin: ItemSkin,
) : SkinMeta {
    override fun generate(context: SchemeGenerationContext): ItemSkin? {
        return itemSkin
    }
}

private data object DefaultSkinMeta : SkinMeta {
    override fun generate(context: SchemeGenerationContext): ItemSkin? {
        return null
    }
}

internal class SkinMetaSerializer : SchemeItemMetaSerializer<SkinMeta> {
    override val defaultValue: SkinMeta = DefaultSkinMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SkinMeta {
        return DefaultSkinMeta // TODO returns a non-empty value
    }
}