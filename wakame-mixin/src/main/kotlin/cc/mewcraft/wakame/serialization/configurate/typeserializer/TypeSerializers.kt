package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection


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
    .register(KoishRegistries2.ITEM.holderByNameTypeSerializer())
    .register(KoishRegistries2.ITEM_PROXY.holderByNameTypeSerializer())
    .build()

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