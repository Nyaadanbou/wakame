package cc.mewcraft.wakame.registries

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.attribute.composite.VariableCompositeAttribute
import cc.mewcraft.wakame.core.Registry
import cc.mewcraft.wakame.core.ResourceKey
import cc.mewcraft.wakame.core.ResourceLocations
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.EntityTypeHolder
import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.components.ItemSkin
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.LevelMapping
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.registry.CompositeAttributeFacade

object KoishRegistryKeys {
    @JvmField
    val ROOT_REGISTRY_NAME = ResourceLocations.withKoishNamespace("root")

    ///

    @JvmField
    val ABILITY = createRegistryKey<Ability>("ability")

    @JvmField
    val ATTRIBUTE = createRegistryKey<Attribute>("attribute")

    @JvmField
    val COMPOSITE_ATTRIBUTE = createRegistryKey<CompositeAttributeFacade<ConstantCompositeAttribute, VariableCompositeAttribute>>("attribute_composition")

    @JvmField
    val ELEMENT = createRegistryKey<Element>("element")

    @JvmField
    val ENTITY_TYPE_HOLDER = createRegistryKey<EntityTypeHolder>("entity_type_holder")

    @JvmField
    val ITEM = createRegistryKey<NekoItem>("item")

    @JvmField
    val VANILLA_PROXY_ITEM = createRegistryKey<NekoItem>("vanilla_proxy_item")

    @JvmField
    val ITEM_SKIN = createRegistryKey<ItemSkin>("item_skin")

    @JvmField
    val KIZAMI = createRegistryKey<Kizami>("kizami")

    @JvmField
    val LEVEL_RARITY_MAPPING = createRegistryKey<LevelMapping>("level_rarity_mapping")

    @JvmField
    val RARITY = createRegistryKey<Rarity>("rarity")

    ///

    private fun <T> createRegistryKey(name: String): ResourceKey<out Registry<T>> {
        return ResourceKey.create(ROOT_REGISTRY_NAME, ResourceLocations.withKoishNamespace(name))
    }
}