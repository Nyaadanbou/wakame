package cc.mewcraft.wakame.serialization.configurate

import cc.mewcraft.wakame.molang.ExpressionSerializer
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.serialization.configurate.typeserializer.AttributeModifierSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.ComponentSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.DispatchingTypeSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.EntityTypeSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.IntRangeSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.KeySerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.MaterialSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.NamespacedKeySerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.PotionEffectSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.StyleBuilderApplicableSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.StyleSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.UnitSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.Vector3fSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.holderByNameTypeSerializer
import cc.mewcraft.wakame.serialization.configurate.typeserializer.valueByNameTypeSerializer
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import kotlin.reflect.KClass


/**
 * 本集合包含会在项目多个地方使用的 [TypeSerializer].
 *
 * 如果一个 [TypeSerializer] 只会在一个地方使用, 那么它应该直接在使用它的
 * [org.spongepowered.configurate.loader.ConfigurationLoader] 中注册.
 */
val KOISH_CONFIGURATE_SERIALIZERS_2: TypeSerializerCollection = TypeSerializerCollection.builder()
    // Kotlin Object
    .register(UnitSerializer)
    // Koish Object
    .register(AttributeModifierSerializer)
    // Adventure Text
    .register(ComponentSerializer)
    .register(StyleSerializer)
    .register(StyleBuilderApplicableSerializer)
    .register(IntRangeSerializer)
    // Namespaced
    .register(KeySerializer)
    .register(NamespacedKeySerializer)
    // Math
    .register(Vector3fSerializer)
    // Bukkit Object
    .register(PotionEffectSerializer)
    .register(ExpressionSerializer)
    // Bukkit Enum
    .register(EntityTypeSerializer)
    .register(MaterialSerializer)
    // Paper Registry
    .register(RegistryKey.DAMAGE_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ENCHANTMENT.valueByNameTypeSerializer())
    .register(RegistryKey.ENTITY_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ITEM.valueByNameTypeSerializer())
    .register(RegistryKey.MOB_EFFECT.valueByNameTypeSerializer())
    // Koish Registry
    .register(KoishRegistries2.ABILITY_META.valueByNameTypeSerializer())
    .register(KoishRegistries2.ABILITY_META_TYPE.valueByNameTypeSerializer())
    .register(KoishRegistries2.ABILITY_TRIGGER.valueByNameTypeSerializer())
    .register(KoishRegistries2.ITEM.holderByNameTypeSerializer())
    .register(KoishRegistries2.ITEM_PROXY.holderByNameTypeSerializer())
    .build()

object TypeSerializers {

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类的反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatchPartial(
        decodingMap: Map<K, KClass<out V>>,
    ): TypeSerializer<V> {
        return dispatchPartial("type", decodingMap)
    }

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类的反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatchPartial(
        typeKey: String,
        decodingMap: Map<K, KClass<out V>>,
    ): TypeSerializer<V> {
        return dispatchPartial<K, V>(typeKey) { key ->
            decodingMap[key]?.let { TypeToken.get(it.java) }
                ?: throw SerializationException("No type mapping found for key: $key (type: ${key::class})")
        }
    }

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类的反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatchPartial(
        noinline decodingMap: (K) -> TypeToken<out V>,
    ): TypeSerializer<V> {
        return dispatchPartial("type", decodingMap)
    }

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类的反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatchPartial(
        typeKey: String,
        noinline decodingMap: (K) -> TypeToken<out V>,
    ): TypeSerializer<V> {
        return DispatchingTypeSerializer(
            typeKey,
            typeTokenOf<K>(),
            { throw UnsupportedOperationException("Serialization is not supported") },
            decodingMap
        )
    }

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类型的序列化/反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatch(
        typeKey: String,
        noinline encodingMap: (V) -> K,
        noinline decodingMap: (K) -> TypeToken<out V>,
    ): TypeSerializer<V> {
        return DispatchingTypeSerializer(
            typeKey,
            typeTokenOf<K>(),
            encodingMap,
            decodingMap
        )
    }

    /**
     * 创建一个 [TypeSerializer] 用于处理多态类型的序列化/反序列化.
     */
    inline fun <reified K : Any, reified V : Any> dispatch(
        noinline encodingMap: (V) -> K,
        noinline decodingMap: (K) -> TypeToken<out V>,
    ): TypeSerializer<V> {
        return dispatch("type", encodingMap, decodingMap)
    }

}