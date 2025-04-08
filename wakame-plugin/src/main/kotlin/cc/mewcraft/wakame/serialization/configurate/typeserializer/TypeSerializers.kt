@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.molang.ExpressionSerializer
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.DynamicRegistries
import cc.mewcraft.wakame.serialization.configurate.serializer.*
import cc.mewcraft.wakame.util.RandomizedValueSerializer
import cc.mewcraft.wakame.util.register
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.serialize.TypeSerializerCollection

// FIXME #350: 迁移到 wakame-mixin
/**
 * 这些序列化器可以处理 Koish 内部的数据类型.
 */
val KOISH_SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    // Koish
    .register(AttributeModifierSerializer)
    .register(RandomizedValueSerializer)
    .register(ExpressionSerializer)
    // Text
    .register(ComponentSerializer)
    .register(StyleSerializer)
    .register(StyleBuilderApplicableSerializer)
    // Guava
    .register(IntRangeSerializer)
    // Namespaced
    .register(IdentifierSerializer)
    .register(NamespacedKeySerializer)
    // Math
    .register(Vector3fSerializer)
    // Bukkit Object
    .register(PotionEffectSerializer)
    // Bukkit Enum
    .register(EntityTypeSerializer)
    .register(MaterialSerializer)
    .register(BlockTypeListSerializer)
    .register(ItemTypeListSerializer)
    // Paper Registry
    .register(RegistryKey.DAMAGE_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ENCHANTMENT.valueByNameTypeSerializer())
    .register(RegistryKey.ENTITY_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ITEM.valueByNameTypeSerializer())
    .register(RegistryKey.MOB_EFFECT.valueByNameTypeSerializer())
    // Koish Registry
    .register(BuiltInRegistries.ABILITY_META.holderByNameTypeSerializer())
    .register(BuiltInRegistries.ABILITY_META_TYPE.valueByNameTypeSerializer())
    .register(BuiltInRegistries.ABILITY_TRIGGER.valueByNameTypeSerializer())
    .register(BuiltInRegistries.ATTRIBUTE.holderByNameTypeSerializer())
    .register(BuiltInRegistries.ELEMENT.holderByNameTypeSerializer())
    .register(BuiltInRegistries.ENTITY_REF.holderByNameTypeSerializer())
    .register(DynamicRegistries.ITEM.holderByNameTypeSerializer())
    .register(DynamicRegistries.ITEM_CATEGORY.holderByNameTypeSerializer())
    .register(BuiltInRegistries.KIZAMI.holderByNameTypeSerializer())
    .register(BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.holderByNameTypeSerializer())
    .register(BuiltInRegistries.RARITY.holderByNameTypeSerializer())
    .build()
