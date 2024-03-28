package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.nonGenerate
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.toMetaResult
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 */
sealed interface DisplayLoreMeta : SchemeItemMeta<List<String>> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "lore")
    }
}

private class NonNullDisplayLoreMeta(
    /**
     * The item lore in the format of MiniMessage string.
     */
    private val lore: List<String>,
) : DisplayLoreMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<List<String>> {
        return lore.toMetaResult()
    }
}

private data object DefaultDisplayLoreMeta : DisplayLoreMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<List<String>> = nonGenerate()
}

internal class DisplayLoreMetaSerializer : SchemeItemMetaSerializer<DisplayLoreMeta> {
    override val defaultValue: DisplayLoreMeta = DefaultDisplayLoreMeta
    override fun deserialize(type: Type, node: ConfigurationNode): DisplayLoreMeta {
        return NonNullDisplayLoreMeta(node.getList<String>(emptyList()))
    }
}