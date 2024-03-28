package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.nonGenerate
import cc.mewcraft.wakame.item.scheme.meta.SchemeItemMeta.ResultUtil.toMetaResult
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 */
sealed interface DisplayNameMeta : SchemeItemMeta<String> {
    companion object : Keyed {
        override val key: Key = Key.key(NekoNamespaces.ITEM_META, "display_name")
    }
}

private class NonNullDisplayNameMeta(
    /**
     * The item name in the format of MiniMessage string
     */
    private val displayName: String,
) : DisplayNameMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<String> {
        return displayName.toMetaResult()
    }
}

private data object DefaultDisplayNameMeta : DisplayNameMeta {
    override fun generate(context: SchemeGenerationContext): SchemeItemMeta.Result<String> = nonGenerate()
}

internal class DisplayNameMetaSerializer : SchemeItemMetaSerializer<DisplayNameMeta> {
    override val defaultValue: DisplayNameMeta = DefaultDisplayNameMeta
    override fun deserialize(type: Type, node: ConfigurationNode): DisplayNameMeta {
        return NonNullDisplayNameMeta(node.requireKt<String>())
    }
}