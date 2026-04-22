@file:JvmName("Serializers")

package cc.mewcraft.wakame.util.configurate

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.lazyconfig.configurate.registerExact
import cc.mewcraft.wakame.animation.AnimationData
import cc.mewcraft.wakame.animation.TextBuilder
import cc.mewcraft.wakame.bridge.MojangBuiltInRegistries
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.entity.display.Brightness
import cc.mewcraft.wakame.loot.LootPool
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.loot.entry.ComposableEntryContainer
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.molang.Expression
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.serialization.configurate.serializer.*
import cc.mewcraft.wakame.util.RandomizedValue
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.serialize.TypeSerializerCollection

/**
 * 这些序列化器可以处理 Koish 内部的数据类型.
 */
internal val KOISH_SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    // Koish
    .registerExact(AnimationData.serializer())
    .register(AttributeModifier.serializer())
    .register(Brightness.serializer())
    .register(ComposableEntryContainer.serializer())
    .register(Expression.serializer())
    .register(LootTable.serializer())
    .register(LootPool.serializer())
    .register(LootPredicate.serializer())
    .register(RandomizedValue.serializer())
    .registerExact(TextBuilder.serializer())
    // Kotlin
    .register(IntRangeKotlinSerializer)
    // Text Component
    .register(MiniMessageComponentSerializer)
    .register(MiniMessageStyleSerializer)
    .register(MiniMessageStyleBuilderApplicableSerializer)
    // Guava
    .register(IntRangeGuavaSerializer)
    // Namespaced
    .register(KoishKeySerializer)
    .register(NamespacedKeySerializer)
    .register(IdentifierSerializer)
    // Math
    .register(Vector3fSerializer)
    .register(QuaternionfSerializer)
    .register(TransformationSerializer)
    // Bukkit Object
    .register(LocationSerializer)
    .register(PotionEffectSerializer)
    // Bukkit Enum
    .register(EntityTypeSerializer)
    .register(MaterialSerializer)
    .register(BlockTypeListSerializer)
    .register(ItemTypeListSerializer)
    // NMS Registry
    .register(MojangBuiltInRegistries.DATA_COMPONENT_TYPE.valueByNameTypeSerializer())
    // Paper Registry
    .register(RegistryKey.DAMAGE_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.DATA_COMPONENT_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ENCHANTMENT.valueByNameTypeSerializer())
    .register(RegistryKey.ENTITY_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ITEM.valueByNameTypeSerializer())
    .register(RegistryKey.MOB_EFFECT.valueByNameTypeSerializer())
    .register(RegistryKey.SOUND_EVENT.valueByNameTypeSerializer())
    // Koish Builtin Registry
    .register(BuiltInRegistries.ATTRIBUTE.holderByNameTypeSerializer())
    .register(BuiltInRegistries.ELEMENT.holderByNameTypeSerializer())
    .register(BuiltInRegistries.ENTITY_REF.holderByNameTypeSerializer())
    .register(BuiltInRegistries.KIZAMI.holderByNameTypeSerializer())
    .register(BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.holderByNameTypeSerializer())
    .register(BuiltInRegistries.RARITY.holderByNameTypeSerializer())
    // Koish Dynamic Registry
    .register(DynamicRegistries.CATALOG_ITEM_CATEGORY.holderByNameTypeSerializer())
    .build()
