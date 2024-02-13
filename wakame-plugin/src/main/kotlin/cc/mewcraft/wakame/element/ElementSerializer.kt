package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

/**
 * ## Node structure 1: read from registry
 *
 * ```yaml
 * <node>: neutral
 * ```
 *
 * ## Node structure 2: create from config
 *
 * ```yaml
 * neutral:
 *   binary_index: 0
 *   display_name: 中立
 * ```
 */
internal class ElementSerializer : SchemeSerializer<Element> {
    override fun deserialize(type: Type, node: ConfigurationNode): Element {
        val scalar = node.rawScalar() as? String
        if (scalar != null) {
            // if it's structure 1
            return ElementRegistry.getOrThrow(scalar)
        }

        // if it's structure 2
        val elementName = node.key().toString()
        val binaryIndex = node.node("binary_index").requireKt<Int>().toStableByte()
        val displayName = node.node("display_name").requireKt<String>()
        val element = Element(elementName, binaryIndex, displayName)
        return element
    }
}