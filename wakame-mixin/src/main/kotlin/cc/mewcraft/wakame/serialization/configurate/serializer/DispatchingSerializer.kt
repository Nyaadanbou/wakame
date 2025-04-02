package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Generic Dispatching TypeSerializer for handling polymorphic types in Configurate.
 */
class DispatchingSerializer<K : Any, V : Any>
private constructor(
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

    /**
     * 使用这些函数来创建 [DispatchingSerializer] 的实例.
     */
    companion object {

        /**
         * 创建一个 [TypeSerializer] 用于处理多态类的反序列化.
         */
        inline fun <reified K : Any, reified V : Any> createPartial(
            decodingMap: Map<K, KClass<out V>>,
        ): TypeSerializer2<V> {
            return createPartial("type", decodingMap)
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类的反序列化.
         */
        inline fun <reified K : Any, reified V : Any> createPartial(
            typeKey: String,
            decodingMap: Map<K, KClass<out V>>,
        ): TypeSerializer2<V> {
            return createPartial<K, V>(typeKey) { key ->
                decodingMap[key]?.let { TypeToken.get(it.java) }
                    ?: throw SerializationException("No type mapping found for key: $key (type: ${key::class})")
            }
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类的反序列化.
         */
        inline fun <reified K : Any, reified V : Any> createPartial(
            noinline decodingMap: (K) -> TypeToken<out V>,
        ): TypeSerializer2<V> {
            return createPartial("type", decodingMap)
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类的反序列化.
         */
        inline fun <reified K : Any, reified V : Any> createPartial(
            typeKey: String,
            noinline decodingMap: (K) -> TypeToken<out V>,
        ): TypeSerializer2<V> {
            return DispatchingSerializer(
                typeKey,
                typeTokenOf<K>(),
                { throw UnsupportedOperationException("Serialization is not supported") },
                decodingMap
            )
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类型的序列化/反序列化.
         */
        inline fun <reified K : Any, reified V : Any> create(
            typeKey: String,
            noinline encodingMap: (V) -> K,
            noinline decodingMap: (K) -> TypeToken<out V>,
        ): TypeSerializer2<V> {
            return DispatchingSerializer(
                typeKey,
                typeTokenOf<K>(),
                encodingMap,
                decodingMap
            )
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类型的序列化/反序列化.
         */
        inline fun <reified K : Any, reified V : Any> create(
            noinline encodingMap: (V) -> K,
            noinline decodingMap: (K) -> TypeToken<out V>,
        ): TypeSerializer2<V> {
            return create("type", encodingMap, decodingMap)
        }

    }

    override fun deserialize(type: Type, node: ConfigurationNode): V {
        val typeKeyNode: ConfigurationNode = node.node(this.typeKey)
        if (typeKeyNode.virtual()) {
            throw SerializationException(node, type, "Input does not contain a type key [${typeKey}]")
        }
        val typeKeyValue: K = typeKeyNode.get(this.keyType)
            ?: throw SerializationException(node, type, "Failed to map type name '${typeKeyNode.raw()}' to type '${keyType.type.typeName}'")
        val valueType: TypeToken<out V> = this.decodingLookup(typeKeyValue)
        return node.get(valueType)
            ?: throw SerializationException(node, type, "Input does not have a valid value entry")
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
