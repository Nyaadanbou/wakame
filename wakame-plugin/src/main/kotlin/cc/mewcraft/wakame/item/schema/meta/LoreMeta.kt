package cc.mewcraft.wakame.item.schema.meta

import cc.mewcraft.wakame.annotation.ConfigPath
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 */
@ConfigPath(ItemMetaConstants.LORE)
sealed interface SLoreMeta : SchemaItemMeta<List<String>> {
    override val key: Key get() = ItemMetaConstants.createKey { LORE }
}

private class NonNullLoreMeta(
    /**
     * The item lore in the format of MiniMessage string.
     */
    private val lore: List<String>,
) : SLoreMeta {
    override val isEmpty: Boolean = false
    override fun generate(context: SchemaGenerationContext): GenerationResult<List<String>> {
        return GenerationResult(lore)
    }
}

private data object DefaultLoreMeta : SLoreMeta {
    override val isEmpty: Boolean = true
    override fun generate(context: SchemaGenerationContext): GenerationResult<List<String>> = GenerationResult.empty()
}

internal data object LoreMetaSerializer : SchemaItemMetaSerializer<SLoreMeta> {
    override val defaultValue: SLoreMeta = DefaultLoreMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SLoreMeta {
        return NonNullLoreMeta(node.getList<String>(emptyList()))
    }
}