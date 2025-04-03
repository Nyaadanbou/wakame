@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.molang.ExpressionSerializer
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.RandomizedValueSerializer
import cc.mewcraft.wakame.util.register
import io.papermc.paper.registry.RegistryKey
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection

// FIXME #350: 迁移到 wakame-mixin
/**
 * 本集合包含会在项目多个地方使用的 [TypeSerializer].
 *
 * 如果一个 [TypeSerializer] 只会在一个地方使用, 那么它应该直接在使用它的
 * [org.spongepowered.configurate.loader.ConfigurationLoader] 中注册.
 */
val KOISH_CONFIGURATE_SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
    // Kotlin Object
    .register(UnitSerializer)
    // Koish Object
    .register(AttributeModifierSerializer)
    .register(RandomizedValueSerializer)
    .register(ExpressionSerializer)
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
    .register(BlockTypeListTypeSerializer)
    .register(ItemTypeListTypeSerializer)
    // Paper Registry
    .register(RegistryKey.DAMAGE_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ENCHANTMENT.valueByNameTypeSerializer())
    .register(RegistryKey.ENTITY_TYPE.valueByNameTypeSerializer())
    .register(RegistryKey.ITEM.valueByNameTypeSerializer())
    .register(RegistryKey.MOB_EFFECT.valueByNameTypeSerializer())
    // Koish Registry
    .register(KoishRegistries2.ABILITY_META.holderByNameTypeSerializer())
    .register(KoishRegistries2.ABILITY_META_TYPE.valueByNameTypeSerializer())
    .register(KoishRegistries2.ABILITY_TRIGGER.valueByNameTypeSerializer())
    .register(KoishRegistries.ATTRIBUTE.holderByNameTypeSerializer())
    .register(KoishRegistries.ELEMENT.holderByNameTypeSerializer())
    .register(KoishRegistries.ENTITY_TYPE_HOLDER.holderByNameTypeSerializer())
    .register(KoishRegistries.ITEM.holderByNameTypeSerializer())
    .register(KoishRegistries.ITEM_CATEGORY.holderByNameTypeSerializer())
    .register(KoishRegistries.ITEM_SKIN.holderByNameTypeSerializer())
    .register(KoishRegistries.KIZAMI.holderByNameTypeSerializer())
    .register(KoishRegistries.LEVEL_RARITY_MAPPING.holderByNameTypeSerializer())
    .register(KoishRegistries.RARITY.holderByNameTypeSerializer())
    .build()
