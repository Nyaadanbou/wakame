package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.serialize.TypeSerializer


object TypeSerializers {

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类型的序列化/反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatching(
        noinline typeInfoLookup: (V) -> K,
        noinline decodingLookup: (K) -> TypeToken<out V>,
    ): TypeSerializer<V> {
        return dispatching("type", typeInfoLookup, decodingLookup)
    }

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类型的序列化/反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatching(
        typeKey: String,
        noinline typeInfoLookup: (V) -> K,
        noinline decodingLookup: (K) -> TypeToken<out V>,
    ): TypeSerializer<V> {
        return DispatchingTypeSerializer(typeKey, typeTokenOf(), typeInfoLookup, decodingLookup)
    }

}