package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.config.configurate.TypeSerializer2
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.set
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf

/**
 * Generic Dispatching TypeSerializer for handling polymorphic types in Configurate.
 */
class DispatchingSerializer<K : Any, V : Any>
private constructor(
    private val typeKey: String,
    private val keyType: KType, // KType<K>
    private val typeInfoLookup: (V) -> K,
    private val decodingLookup: (K) -> KType, // (K) -> KType<out V>
    private val encodingLookup: (V) -> KType, // (V) -> KType<out V>
) : TypeSerializer2<V> {

    constructor(
        typeKey: String,
        keyType: KType,
        typeInfoLookup: (V) -> K,
        decodingLookup: (K) -> KType,
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
                decodingMap[key]?.starProjectedType // 使用该函数意味着 V 没有任何参数
                    ?: throw SerializationException("No type mapping found for key: $key (type: ${key::class})")
            }
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类的反序列化.
         */
        inline fun <reified K : Any, reified V : Any> createPartial(
            noinline decodingMap: (K) -> KType, // KType<V>
        ): TypeSerializer2<V> {
            return createPartial("type", decodingMap)
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类的反序列化.
         */
        inline fun <reified K : Any, reified V : Any> createPartial(
            typeKey: String,
            noinline decodingMap: (K) -> KType,
        ): TypeSerializer2<V> {
            return DispatchingSerializer(
                typeKey,
                typeOf<K>(),
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
            noinline decodingMap: (K) -> KType, // KType<V>
        ): TypeSerializer2<V> {
            return DispatchingSerializer(
                typeKey,
                typeOf<K>(),
                encodingMap,
                decodingMap
            )
        }

        /**
         * 创建一个 [TypeSerializer2] 用于处理多态类型的序列化/反序列化.
         */
        inline fun <reified K : Any, reified V : Any> create(
            noinline encodingMap: (V) -> K,
            noinline decodingMap: (K) -> KType, // KType<V>
        ): TypeSerializer2<V> {
            return create("type", encodingMap, decodingMap)
        }

    }

    override fun deserialize(type: Type, node: ConfigurationNode): V {
        val typeKeyNode: ConfigurationNode = node.node(this.typeKey)
        if (typeKeyNode.virtual()) {
            throw SerializationException(node, type, "Input does not contain a type key [${this.typeKey}]")
        }
        val typeKeyValue: K = (typeKeyNode.get(this.keyType) as? K)
            ?: throw SerializationException(node, type, "Failed to map type name '${typeKeyNode.raw()}' to type '${this.keyType}'")
        val valueType: KType = this.decodingLookup(typeKeyValue)
        return (node.get(valueType) as? V)
            ?: throw SerializationException(node, type, "Input does not have a valid value entry")
    }

    override fun serialize(type: Type, obj: V?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }

        val typeKeyValue: K = this.typeInfoLookup(obj)
        node.node(this.typeKey).set(this.keyType, typeKeyValue)

        val valueType: KType = this.encodingLookup(obj)
        node.set(valueType, obj)
    }
}
