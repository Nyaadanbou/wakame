package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 */
sealed interface SDisplayLoreMeta : SchemaItemMeta<List<String>>

private class NonNullDisplayLoreMeta(
    /**
     * The item lore in the format of MiniMessage string.
     */
    private val lore: List<String>,
) : SDisplayLoreMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<List<String>> {
        return GenerationResult(lore)
    }
}

private data object DefaultDisplayLoreMeta : SDisplayLoreMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<List<String>> = GenerationResult.empty()
}

internal data object DisplayLoreMetaSerializer : SchemaItemMetaSerializer<SDisplayLoreMeta> {
    override val defaultValue: SDisplayLoreMeta = DefaultDisplayLoreMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SDisplayLoreMeta {
        return NonNullDisplayLoreMeta(node.getList<String>(emptyList()))
    }
}