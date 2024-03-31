package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 */
sealed interface SDisplayLoreMeta : SchemeItemMeta<List<String>> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "lore")
    }
}

private class NonNullDisplayLoreMeta(
    /**
     * The item lore in the format of MiniMessage string.
     */
    private val lore: List<String>,
) : SDisplayLoreMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<List<String>> {
        return GenerationResult(lore)
    }
}

private data object DefaultDisplayLoreMeta : SDisplayLoreMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<List<String>> = GenerationResult.empty()
}

internal class DisplayLoreMetaSerializer : SchemeItemMetaSerializer<SDisplayLoreMeta> {
    override val defaultValue: SDisplayLoreMeta = DefaultDisplayLoreMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SDisplayLoreMeta {
        return NonNullDisplayLoreMeta(node.getList<String>(emptyList()))
    }
}