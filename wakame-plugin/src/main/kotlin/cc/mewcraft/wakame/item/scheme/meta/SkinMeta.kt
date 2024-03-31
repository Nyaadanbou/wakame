package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的皮肤。
 */
sealed interface SSkinMeta : SchemeItemMeta<ItemSkin> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "skin")
    }
}

private class NonNullSkinMeta(
    private val itemSkin: ItemSkin,
) : SSkinMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<ItemSkin> {
        return GenerationResult(itemSkin)
    }
}

private data object DefaultSkinMeta : SSkinMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<ItemSkin> = GenerationResult.empty()
}

internal class SkinMetaSerializer : SchemeItemMetaSerializer<SSkinMeta> {
    override val defaultValue: SSkinMeta = DefaultSkinMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SSkinMeta {
        return DefaultSkinMeta // TODO returns a non-empty value
    }
}