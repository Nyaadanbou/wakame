@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.registries.KoishRegistries
import cc.mewcraft.wakame.util.NumericValueSerializer
import io.leangen.geantyref.TypeToken
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import xyz.xenondevs.commons.reflection.javaTypeOf

val KOISH_CONFIGURATE_SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    // Koish Object
    .register(NumericValueSerializer)
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
    .register(KoishRegistries.COMPOSITE_ATTRIBUTE.holderByNameTypeSerializer())
    .register(KoishRegistries.ELEMENT.holderByNameTypeSerializer())
    .register(KoishRegistries.ENTITY_TYPE_HOLDER.holderByNameTypeSerializer())
    .register(KoishRegistries.ITEM.holderByNameTypeSerializer())
    // .register(KoishRegistries.VANILLA_PROXY_ITEM.holderByNameTypeSerializer())
    .register(KoishRegistries.ITEM_SKIN.holderByNameTypeSerializer())
    .register(KoishRegistries.KIZAMI.holderByNameTypeSerializer())
    .register(KoishRegistries.LEVEL_RARITY_MAPPING.holderByNameTypeSerializer())
    .register(KoishRegistries.RARITY.holderByNameTypeSerializer())
    .build()

private inline fun <reified T> TypeSerializerCollection.Builder.register(serializer: TypeSerializer<T>): TypeSerializerCollection.Builder {
    val type = javaTypeOf<T>()
    register({ it == type }, serializer)
    return this
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T> geantyrefTypeTokenOf(): TypeToken<T> =
    TypeToken.get(javaTypeOf<T>()) as TypeToken<T>