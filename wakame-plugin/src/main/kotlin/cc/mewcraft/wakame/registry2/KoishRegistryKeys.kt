package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.ImaginaryAttributeMap
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.attribute.bundle.VariableAttributeBundle
import cc.mewcraft.wakame.catalog.item.CatalogItemCategory
import cc.mewcraft.wakame.catalog.item.recipe.CatalogItemLootTableRecipe
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacade
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.components.ItemSkin
import cc.mewcraft.wakame.kizami.KizamiType
import cc.mewcraft.wakame.rarity.LevelRarityMapping
import cc.mewcraft.wakame.rarity.RarityType
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.world.entity.EntityTypeHolder

object KoishRegistryKeys {
    @JvmField
    val ROOT_REGISTRY_NAME = Identifiers.of("root")

    ///

    @JvmField
    val ABILITY = createRegistryKey<Ability>("ability")

    @JvmField
    val ABILITY_ARCHETYPE = createRegistryKey<AbilityArchetype>("ability_archetype")

    @JvmField
    val ATTRIBUTE = createRegistryKey<Attribute>("attribute")

    @JvmField
    val ATTRIBUTE_SUPPLIER = createRegistryKey<AttributeSupplier>("attribute_supplier")

    @JvmField
    val ATTRIBUTE_BUNDLE_FACADE = createRegistryKey<AttributeBundleFacade<ConstantAttributeBundle, VariableAttributeBundle>>("attribute_bundle_facade")

    @JvmField
    val ELEMENT = createRegistryKey<ElementType>("element")

    @JvmField
    val ENTITY_TYPE_HOLDER = createRegistryKey<EntityTypeHolder>("entity_type_holder")

    @JvmField
    val IMAGINARY_ATTRIBUTE_MAP = createRegistryKey<ImaginaryAttributeMap>("imaginary_attribute_map")

    @JvmField
    val ITEM = createRegistryKey<NekoItem>("item")

    @JvmField
    val ITEM_SKIN = createRegistryKey<ItemSkin>("item_skin")

    @JvmField
    val ITEM_CATEGORY = createRegistryKey<CatalogItemCategory>("item_category")

    @JvmField
    val KIZAMI = createRegistryKey<KizamiType>("kizami")

    @JvmField
    val LEVEL_RARITY_MAPPING = createRegistryKey<LevelRarityMapping>("level_rarity_mapping")

    @JvmField
    val LOOT_TABLE_RECIPE = createRegistryKey<CatalogItemLootTableRecipe>("loot_table_recipe")

    @JvmField
    val RARITY = createRegistryKey<RarityType>("rarity")

    @JvmField
    val TRIGGER = createRegistryKey<Trigger>("trigger")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.Companion.of(ROOT_REGISTRY_NAME, Identifiers.of(name))
    }
}