package cc.mewcraft.wakame.serialization.configurate.extension

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.ScalarSerializer

inline fun <reified U> Map<Any, ConfigurationNode>.transformKeys(): Map<U, ConfigurationNode> {
    return this.mapKeys { (nodeKey, node) ->
        val serializer = node.options().serializers().get<U>()
        if (serializer is ScalarSerializer<U>) {
            serializer.deserialize(nodeKey)
        } else {
            throw IllegalStateException("No such scalar serializer for type '${U::class}' to parse ${node.raw()}")
        }
    }
}