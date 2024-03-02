package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 */
interface DisplayLoreMeta : SchemeItemMeta<List<String>> {
    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "lore")
    }
}

private class NonNullDisplayLoreMeta(
    /**
     * The item lore in the format of MiniMessage string.
     */
    private val lore: List<String>,
) : DisplayLoreMeta {
    override fun generate(context: SchemeGenerationContext): List<String> {
        return lore
    }
}

private object DefaultDisplayLoreMeta : DisplayLoreMeta {
    override fun generate(context: SchemeGenerationContext): List<String>? = null
}

internal class DisplayLoreMetaSerializer : SchemeItemMetaSerializer<DisplayLoreMeta> {
    override val defaultValue: DisplayLoreMeta = DefaultDisplayLoreMeta
    override fun deserialize(type: Type, node: ConfigurationNode): DisplayLoreMeta {
        return NonNullDisplayLoreMeta(node.getList<String>(emptyList()))
    }
}