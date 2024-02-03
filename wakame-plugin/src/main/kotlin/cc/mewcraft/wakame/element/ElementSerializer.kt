package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.toStableByte
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

internal class ElementSerializer : TypeSerializer<Element> {
    override fun deserialize(type: Type, node: ConfigurationNode): Element {
        val elementName = node.key().toString()
        val binaryIndex = node.node("binary_index").require<Int>().toStableByte()
        val displayName = node.node("display_name").require<String>()
        val element = Element(elementName, binaryIndex, displayName)
        return element
    }

    override fun serialize(type: Type, obj: Element?, node: ConfigurationNode) {
        TODO("Not yet implemented")
    }
}