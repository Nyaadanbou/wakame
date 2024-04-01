package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 */
sealed interface SDisplayNameMeta : SchemaItemMeta<String>

private class NonNullDisplayNameMeta(
    /**
     * The item name in the format of MiniMessage string
     */
    private val displayName: String,
) : SDisplayNameMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> {
        return GenerationResult(displayName)
    }
}

private data object DefaultDisplayNameMeta : SDisplayNameMeta {
    override fun generate(context: SchemaGenerationContext): GenerationResult<String> = GenerationResult.empty()
}

internal data object DisplayNameMetaSerializer : SchemaItemMetaSerializer<SDisplayNameMeta> {
    override val defaultValue: SDisplayNameMeta = DefaultDisplayNameMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SDisplayNameMeta {
        return NonNullDisplayNameMeta(node.requireKt<String>())
    }
}