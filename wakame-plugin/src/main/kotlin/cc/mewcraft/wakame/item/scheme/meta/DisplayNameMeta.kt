package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.typedRequire
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
    private val displayName: String,
) : SchemeMeta<Component>, KoinComponent {
    private val miniMessage: MiniMessage by inject(named(MINIMESSAGE_FULL))

    override fun generate(context: SchemeGenerationContext): Component {
        return miniMessage.deserialize(displayName)
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(SchemeMeta.ITEM_META_NAMESPACE, "display_name")
    }
}

internal class DisplayNameMetaSerializer : SchemeSerializer<DisplayNameMeta> {
    override fun deserialize(type: Type, node: ConfigurationNode): DisplayNameMeta {
        return DisplayNameMeta(node.typedRequire<String>())
    }
}