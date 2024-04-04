package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.ItemMetaKeys
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 */
sealed interface SDisplayLoreMeta : SchemaItemMeta<List<String>> {
    override val key: Key get() = ItemMetaKeys.DISPLAY_LORE
}

private class NonNullDisplayLoreMeta(
    /**
     * The item lore in the format of MiniMessage string.
     */
    private val lore: List<String>,
) : SDisplayLoreMeta {
    override val isEmpty: Boolean = false
    override fun generate(context: SchemaGenerationContext): GenerationResult<List<String>> {
        return GenerationResult(lore)
    }
}

private data object DefaultDisplayLoreMeta : SDisplayLoreMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<List<String>> = GenerationResult.empty()
}

internal data object DisplayLoreMetaSerializer : SchemaItemMetaSerializer<SDisplayLoreMeta> {
    override val defaultValue: SDisplayLoreMeta = DefaultDisplayLoreMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SDisplayLoreMeta {
        return NonNullDisplayLoreMeta(node.getList<String>(emptyList()))
    }
}