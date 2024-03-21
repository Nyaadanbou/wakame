package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.util.requireKt
import cc.mewcraft.wakame.util.toStableByte
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * ## Node structure 1: read from registry
 *
 * ```yaml
 * <node>: epic
 * ```
 *
 * ## Node structure 2: create from config
 *
 * ```yaml
 * epic:
 *   binary_index: 3
 *   display_name: 史诗
 *   ...
 * ```
 */
internal class RaritySerializer : SchemeSerializer<Rarity> {
    override fun deserialize(type: Type, node: ConfigurationNode): Rarity {
        val scalar = node.rawScalar() as? String
        if (scalar != null) {
            // if it's structure 1
            return RarityRegistry.INSTANCES.get(scalar)
        }

        // if it's structure 2
        val key = node.key().toString()
        val binary = node.node("binary_index").requireKt<Int>().toStableByte()
        val displayName = node.node("display_name").requireKt<Component>()
        val styles = node.node("styles").requireKt<Array<StyleBuilderApplicable>>()
        return (@OptIn(InternalApi::class) Rarity(key, binary, displayName, styles))
    }
}