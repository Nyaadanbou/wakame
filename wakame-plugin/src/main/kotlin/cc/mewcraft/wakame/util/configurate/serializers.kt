@file:JvmName("Serializers")

package cc.mewcraft.wakame.util.configurate

import cc.mewcraft.lazyconfig.configurate.register
import cc.mewcraft.wakame.loot.LootPool
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.loot.entry.ComposableEntryContainer
import cc.mewcraft.wakame.molang.ExpressionSerializer
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.serialization.configurate.serializer.*
import cc.mewcraft.wakame.util.RandomizedValueSerializer
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
    // Kotlin
    .register(IntRangeKotlinSerializer)
    // Text
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
    // Bukkit Object
    .register(LocationSerializer)
    .register(PotionEffectSerializer)
    // Bukkit Enum
    .register(EntityTypeSerializer)
    .register(MaterialSerializer)
    .register(BlockTypeListSerializer)
    .register(ItemTypeListSerializer)
    // Loot Table
    .register(LootTable.SERIALIZER)
    .register(LootPool.SERIALIZER)
    .register(ComposableEntryContainer.SERIALIZER)
    // NMS Registry
    .register(net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE.valueByNameTypeSerializer())
    // Paper Registry
    .register(RegistryKey.DAMAGE_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.DATA_COMPONENT_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ENCHANTMENT.valueByNameTypeSerializer())
    .register(RegistryKey.ENTITY_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ITEM.valueByNameTypeSerializer())
    .register(RegistryKey.MOB_EFFECT.valueByNameTypeSerializer())
    // Koish Registry
    .register(BuiltInRegistries.ATTRIBUTE.holderByNameTypeSerializer())
    .register(BuiltInRegistries.ELEMENT.holderByNameTypeSerializer())
    .register(BuiltInRegistries.ENTITY_REF.holderByNameTypeSerializer())
    .register(DynamicRegistries.ITEM_CATEGORY.holderByNameTypeSerializer())
    .register(BuiltInRegistries.KIZAMI.holderByNameTypeSerializer())
    .register(BuiltInRegistries.LEVEL_TO_RARITY_MAPPING.holderByNameTypeSerializer())
    .register(BuiltInRegistries.RARITY.holderByNameTypeSerializer())
    .build()
