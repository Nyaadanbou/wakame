package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 */
interface DisplayNameMeta : SchemeItemMeta<String> {
    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "display_name")
    }
}

private class NonNullDisplayNameMeta(
    /**
     * The item name in the format of MiniMessage string
     */
    private val displayName: String,
) : DisplayNameMeta {
    override fun generate(context: SchemeGenerationContext): String {
        return displayName
    }
}

private object DefaultDisplayNameMeta : DisplayNameMeta {
    override fun generate(context: SchemeGenerationContext): String? = null
}

internal class DisplayNameMetaSerializer : SchemeItemMetaSerializer<DisplayNameMeta> {
    override val defaultValue: DisplayNameMeta = DefaultDisplayNameMeta
    override fun deserialize(type: Type, node: ConfigurationNode): DisplayNameMeta {
        return NonNullDisplayNameMeta(node.requireKt<String>())
    }
}