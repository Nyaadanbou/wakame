package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * 物品的名字。
 *
 * @property displayName the item name in the format of MiniMessage string
 */
class DisplayNameMeta(
    private val displayName: String = "Unnamed",
) : SchemeMeta<Component>, KoinComponent {
    private val miniMessage: MiniMessage by inject(named(MINIMESSAGE_FULL), mode = LazyThreadSafetyMode.NONE)

    override fun generate(context: SchemeGenerationContext): Component {
        return miniMessage.deserialize(displayName)
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "display_name")
    }
}

internal class DisplayNameMetaSerializer : SchemeMetaSerializer<DisplayNameMeta> {
    override val emptyValue: DisplayNameMeta = DisplayNameMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): DisplayNameMeta {
        return DisplayNameMeta(node.requireKt<String>())
    }
}