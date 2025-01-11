@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.ability.TriggerVariantSerializer
import cc.mewcraft.wakame.ability.trigger.AbilityTriggerSerializer
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.molang.EvaluableSerializer
import cc.mewcraft.wakame.util.RandomizedValueSerializer
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.reflection.javaTypeOf

/**
 * 本集合包含会在项目多个地方使用的 [TypeSerializer].
 *
 * 如果一个 [TypeSerializer] 只会在一个地方使用, 那么它应该直接在使用它的
 * [org.spongepowered.configurate.loader.ConfigurationLoader] 中注册.
 */
val KOISH_CONFIGURATE_SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    // Koish Object
    .register(AttributeModifierSerializer)
    .register(RandomizedValueSerializer)
    .register(AbilityTriggerSerializer)
    .register(TriggerVariantSerializer)
    .register(EvaluableSerializer)
    // Adventure Text
    .register(ComponentSerializer)
    .register(StyleSerializer)
    .register(StyleBuilderApplicableSerializer)
    .register(IntRangeSerializer)
    // Namespaced
    .register(KeySerializer)
    .register(NamespacedKeySerializer)
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
    .register(KoishRegistries.ABILITY.holderByNameTypeSerializer())
    .register(KoishRegistries.ATTRIBUTE.holderByNameTypeSerializer())
    // .register(KoishRegistries.ATTRIBUTE_FACADE.holderByNameTypeSerializer())
    .register(KoishRegistries.ELEMENT.holderByNameTypeSerializer())
    .register(KoishRegistries.ENTITY_TYPE_HOLDER.holderByNameTypeSerializer())
    .register(KoishRegistries.ITEM.holderByNameTypeSerializer())
    // .register(KoishRegistries.VANILLA_PROXY_ITEM.holderByNameTypeSerializer())
    .register(KoishRegistries.ITEM_SKIN.holderByNameTypeSerializer())
    .register(KoishRegistries.KIZAMI.holderByNameTypeSerializer())
    .register(KoishRegistries.LEVEL_RARITY_MAPPING.holderByNameTypeSerializer())
    .register(KoishRegistries.RARITY.holderByNameTypeSerializer())
    .build()

object Serializers {

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

private inline fun <reified T> TypeSerializerCollection.Builder.register(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder {
    val type = javaTypeOf<T>()
    register({ it == type }, serializer)
    return this
}
