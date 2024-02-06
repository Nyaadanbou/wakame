package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 *
 * @property lore the item lore in the format of MiniMessage string
 */
class LoreMeta(
    private val lore: List<String>,
) : SchemeMeta<List<Component>>, KoinComponent {
    private val miniMessage: MiniMessage by inject()

    override fun generate(context: SchemeGenerationContext): List<Component>? {
        return lore.map { miniMessage.deserialize(it) }.takeIf { it.isNotEmpty() }
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(SchemeMeta.ITEM_META_NAMESPACE, "lore")
    }
}

internal class LoreMetaSerializer : SchemeSerializer<LoreMeta> {
    override fun deserialize(type: Type, node: ConfigurationNode): LoreMeta {
        if (node.virtual()) { // make it optional
            return LoreMeta(emptyList())
        }

        return LoreMeta(node.getList(String::class, emptyList()))
    }
}