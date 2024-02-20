package cc.mewcraft.wakame.item.scheme.meta

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.koin.core.component.KoinComponent
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 物品的描述。
 *
 * @property lore the item lore in the format of MiniMessage string
 */
class LoreMeta(
    private val lore: List<String>? = null,
) : SchemeMeta<List<String>>, KoinComponent {
    override fun generate(context: SchemeGenerationContext): List<String>? {
        return lore
    }

    companion object : Keyed {
        override fun key(): Key = Key.key(NekoNamespaces.META, "lore")
    }
}

internal class LoreMetaSerializer : SchemeMetaSerializer<LoreMeta> {
    override val emptyValue: LoreMeta = LoreMeta()

    override fun deserialize(type: Type, node: ConfigurationNode): LoreMeta {
        return LoreMeta(node.getList<String>(emptyList()))
    }
}