package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ecs.FamiliesBootstrapper
import cc.mewcraft.wakame.ecs.SystemBootstrapper
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.Attribute
import cc.mewcraft.wakame.entity.attribute.AttributeSupplier
import cc.mewcraft.wakame.entity.attribute.ImaginaryAttributeMap
import cc.mewcraft.wakame.entity.attribute.bundle.AttributeFacade
import cc.mewcraft.wakame.entity.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.entity.attribute.bundle.VariableAttributeBundle
import cc.mewcraft.wakame.entity.player.AttackSpeed
import cc.mewcraft.wakame.entity.typeref.EntityRef
import cc.mewcraft.wakame.entity.typeref.EntityRefLookup
import cc.mewcraft.wakame.item2.ItemRefHandler
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishItemProxy
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.item2.data.impl.CoreType
import cc.mewcraft.wakame.item2.data.impl.EntityBucketInfoType
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.loot.LootTable
import cc.mewcraft.wakame.loot.entry.LootPoolEntryType
import cc.mewcraft.wakame.loot.predicate.LootPredicateType
import cc.mewcraft.wakame.rarity2.LevelToRarityMapping
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.util.Identifiers

object BuiltInRegistryKeys {
    @JvmField
    val ROOT_REGISTRY_NAME = Identifiers.of("built_in")

    ///

    @JvmField
    val ABILITY_META = createRegistryKey<AbilityMeta>("ability_meta")

    @JvmField
    val ABILITY_META_TYPE = createRegistryKey<AbilityMetaType<*>>("ability_meta")

    @JvmField
    val ABILITY_TRIGGER = createRegistryKey<AbilityTrigger>("ability_trigger")

    @JvmField
    val ITEM = createRegistryKey<KoishItem>("item")

    @JvmField
    val ITEM_DATA_TYPE = createRegistryKey<ItemDataType<*>>("item_data_type")

    @JvmField
    val ITEM_META_TYPE = createRegistryKey<ItemMetaType<*, *>>("item_meta_type")

    @JvmField
    val ITEM_PROPERTY_TYPE = createRegistryKey<ItemPropertyType<*>>("item_property_type")

    @JvmField
    val ITEM_BEHAVIOR = createRegistryKey<ItemBehavior>("item_behavior")

    @JvmField
    val ITEM_PROXY = createRegistryKey<KoishItemProxy>("item_proxy")

    @JvmField
    val ITEM_REF_HANDLER = createRegistryKey<ItemRefHandler<*>>("item_ref_handler")

    @JvmField
    val ITEM_REF_HANDLER_INTERNAL = createRegistryKey<ItemRefHandler<*>>("item_ref_handler_internal")

    @JvmField
    val RARITY = createRegistryKey<Rarity>("rarity")

    @JvmField
    val KIZAMI = createRegistryKey<Kizami>("kizami")

    @JvmField
    val ENTITY_REF_LOOKUP_DIR = createRegistryKey<EntityRefLookup.Dictionary>("entity_ref_lookup_dir")

    @JvmField
    val ENTITY_REF = createRegistryKey<EntityRef>("entity_ref")

    @JvmField
    val ELEMENT = createRegistryKey<Element>("element")

    @JvmField
    val LEVEL_TO_RARITY_MAPPING = createRegistryKey<LevelToRarityMapping>("level_to_rarity_mapping")

    @JvmField
    val ATTRIBUTE = createRegistryKey<Attribute>("attribute")

    @JvmField
    val ATTRIBUTE_SUPPLIER = createRegistryKey<AttributeSupplier>("attribute_supplier")

    @JvmField
    val ATTRIBUTE_FACADE = createRegistryKey<AttributeFacade<ConstantAttributeBundle, VariableAttributeBundle>>("attribute_facade")

    @JvmField
    val IMG_ATTRIBUTE_MAP = createRegistryKey<ImaginaryAttributeMap>("img_attribute_map")

    @JvmField
    val CORE_TYPE = createRegistryKey<CoreType>("core_type")

    @JvmField
    val ENTITY_BUCKET_INFO_TYPE = createRegistryKey<EntityBucketInfoType>("entity_bucket_info_type")

    @JvmField
    val ATTACK_SPEED = createRegistryKey<AttackSpeed>("attack_speed")

    @JvmField
    val LOOT_TABLE = createRegistryKey<LootTable<*>>("loot_table")

    @JvmField
    val LOOT_POOL_ENTRY_TYPE = createRegistryKey<LootPoolEntryType<*>>("loot_pool_entry_type")

    @JvmField
    val LOOT_PREDICATE_TYPE = createRegistryKey<LootPredicateType<*>>("loot_predicate_type")

    @JvmField
    val SYSTEM_BOOTSTRAPPER = createRegistryKey<SystemBootstrapper>("system_bootstrapper")

    @JvmField
    val FAMILIES_BOOTSTRAPPER = createRegistryKey<FamiliesBootstrapper>("families")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, Identifiers.of(name))
    }
}
