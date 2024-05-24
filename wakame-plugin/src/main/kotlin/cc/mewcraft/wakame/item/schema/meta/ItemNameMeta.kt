package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 */
@ConfigPath(ItemMetaConstants.ITEM_NAME)
sealed interface SItemNameMeta : SchemaItemMeta<String> {
    override val key: Key get() = ItemMetaConstants.createKey { ITEM_NAME }
}

private class NonNullItemNameMeta(
    /**
     * The item name in the format of MiniMessage string
     */
    private val itemName: String,
) : SItemNameMeta {
    override val isEmpty: Boolean = false
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> {
        return GenerationResult(itemName)
    }
}

private data object DefaultItemNameMeta : SItemNameMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> = GenerationResult.empty()
}

internal data object ItemNameMetaSerializer : SchemaItemMetaSerializer<SItemNameMeta> {
    override val defaultValue: SItemNameMeta = DefaultItemNameMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SItemNameMeta {
        return NonNullItemNameMeta(node.krequire<String>())
    }
}