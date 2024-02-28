package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.registry.KizamiRegistry
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
 * <node>: iron
 * ```
 *
 * ## Node structure 2: create from config
 *
 * ```yaml
 * iron:
 *   binary_index: 0
 *   display_name: 中立
 *   ...
 * ```
 */
internal class KizamiSerializer : SchemeSerializer<Kizami> {
    override fun deserialize(type: Type, node: ConfigurationNode): Kizami {
        val scalar = node.rawScalar() as? String
        if (scalar != null) {
            // if it's structure 1
            return KizamiRegistry.getOrThrow(scalar)
        }

        // if it's structure 2
        val key = node.key().toString()
        val binary = node.node("binary_index").requireKt<Int>().toStableByte()
        val displayName = node.node("display_name").requireKt<Component>()
        val styles = node.node("styles").requireKt<Array<StyleBuilderApplicable>>()
        return (@OptIn(InternalApi::class) Kizami(key, binary, displayName, styles))
    }
}