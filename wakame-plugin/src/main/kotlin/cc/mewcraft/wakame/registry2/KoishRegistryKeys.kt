package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.attribute.bundle.VariableAttributeBundle
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
    val ITEM = createRegistryKey<NekoItem>("item")

    // @JvmField
    // val VANILLA_PROXY_ITEM = createRegistryKey<NekoItem>("vanilla_proxy_item")

    @JvmField
    val ITEM_SKIN = createRegistryKey<ItemSkin>("item_skin")

    @JvmField
    val KIZAMI = createRegistryKey<KizamiType>("kizami")

    @JvmField
    val LEVEL_RARITY_MAPPING = createRegistryKey<LevelRarityMapping>("level_rarity_mapping")

    @JvmField
    val RARITY = createRegistryKey<RarityType>("rarity")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.Companion.of(ROOT_REGISTRY_NAME, Identifiers.of(name))
    }
}