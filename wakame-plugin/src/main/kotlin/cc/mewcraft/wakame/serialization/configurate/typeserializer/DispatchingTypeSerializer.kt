package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * Generic Dispatching TypeSerializer for handling polymorphic types in Configurate.
 */
class DispatchingTypeSerializer<K : Any, V : Any>(
    private val typeKey: String,
    private val keyType: TypeToken<K>,
    private val typeInfoLookup: (V) -> K,
    private val decodingLookup: (K) -> TypeToken<out V>,
    private val encodingLookup: (V) -> TypeToken<out V>,
) : TypeSerializer<V> {

    constructor(
        typeKey: String,
        keyType: TypeToken<K>,
        typeInfoLookup: (V) -> K,
        decodingLookup: (K) -> TypeToken<out V>,
    ) : this(
        typeKey,
        keyType,
        typeInfoLookup,
        decodingLookup,
        { v -> decodingLookup(typeInfoLookup(v)) } // 我们可以推算出这个 lambda
    )

    override fun deserialize(type: Type, node: ConfigurationNode): V {
        val typeKeyNode: ConfigurationNode = node.node(this.typeKey)
        if (typeKeyNode.virtual()) {
            throw SerializationException(node, type, "Input does not contain a node key of type: '${typeKey}'")
        }
        val typeKeyValue: K = typeKeyNode.get(this.keyType) ?: throw SerializationException(node, type, "Failed to map type name '${typeKeyNode.raw()}' to type ${keyType.type.typeName}")
        val valueType: TypeToken<out V> = this.decodingLookup(typeKeyValue)
        return node.get(valueType) ?: throw SerializationException(node, type, "Input does not have a value entry")
    }

    override fun serialize(type: Type, obj: V?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }

        val typeKeyValue: K = this.typeInfoLookup(obj)
        node.node(this.typeKey).set(typeKeyValue)

        val valueType = this.encodingLookup(obj)
        node.set(valueType.type, obj)
    }
}
