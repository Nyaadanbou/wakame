package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 */
sealed interface SDisplayNameMeta : SchemeItemMeta<String> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "display_name")
    }
}

private class NonNullDisplayNameMeta(
    /**
     * The item name in the format of MiniMessage string
     */
    private val displayName: String,
) : SDisplayNameMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<String> {
        return GenerationResult(displayName)
    }
}

private data object DefaultDisplayNameMeta : SDisplayNameMeta {
    override fun generate(context: SchemeGenerationContext): GenerationResult<String> = GenerationResult.empty()
}

internal class DisplayNameMetaSerializer : SchemeItemMetaSerializer<SDisplayNameMeta> {
    override val defaultValue: SDisplayNameMeta = DefaultDisplayNameMeta
    override fun deserialize(type: Type, node: ConfigurationNode): SDisplayNameMeta {
        return NonNullDisplayNameMeta(node.requireKt<String>())
    }
}