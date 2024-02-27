package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 *
 * @property displayName the item name in the format of MiniMessage string
 */
data class DisplayNameMeta(
    private val displayName: String = "Unnamed",
) : SchemeItemMeta<String>, KoinComponent {
    override fun generate(context: SchemeGenerationContext): String {
        return displayName
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.ITEM_META, "display_name")
    }
}

internal class DisplayNameMetaSerializer : SchemeItemMetaSerializer<DisplayNameMeta> {
    override val emptyValue: DisplayNameMeta = DisplayNameMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): DisplayNameMeta {
        return DisplayNameMeta(node.requireKt<String>())
    }
}