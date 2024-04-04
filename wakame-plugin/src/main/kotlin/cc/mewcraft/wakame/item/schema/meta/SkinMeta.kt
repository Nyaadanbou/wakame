package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.skin.ItemSkin
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的皮肤。
 */
sealed interface SSkinMeta : SchemaItemMeta<ItemSkin> {
    override val key: Key get() = ItemMetaKeys.SKIN
}

private class NonNullSkinMeta(
    private val itemSkin: ItemSkin,
) : SSkinMeta {
    override val isEmpty: Boolean = false
    override fun generate(context: SchemaGenerationContext): GenerationResult<ItemSkin> {
        return GenerationResult(itemSkin)
    }
}

private data object DefaultSkinMeta : SSkinMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<ItemSkin> = GenerationResult.empty()
}

internal data object SkinMetaSerializer : SchemaItemMetaSerializer<SSkinMeta> {
    override val defaultValue: SSkinMeta = DefaultSkinMeta

    override fun deserialize(type: Type, node: ConfigurationNode): SSkinMeta {
        return DefaultSkinMeta // TODO returns a non-empty value
    }
}