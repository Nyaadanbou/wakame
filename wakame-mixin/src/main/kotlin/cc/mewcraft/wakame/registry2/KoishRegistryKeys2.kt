package cc.mewcraft.wakame.registry2

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.typeref.EntityRef
import cc.mewcraft.wakame.item2.ItemRefHandler
import cc.mewcraft.wakame.item2.KoishItem
import cc.mewcraft.wakame.item2.KoishItemProxy
import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaType
import cc.mewcraft.wakame.item2.config.property.ItemPropertyType
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.kizami2.Kizami
import cc.mewcraft.wakame.rarity2.LevelToRarityMapping
import cc.mewcraft.wakame.rarity2.Rarity
import cc.mewcraft.wakame.util.Identifiers

object KoishRegistryKeys2 {
    @JvmField
    val ROOT_REGISTRY_NAME = Identifiers.of("root_2")

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
    val RARITY = createRegistryKey<Rarity>("rarity")

    @JvmField
    val KIZAMI = createRegistryKey<Kizami>("kizami")

    @JvmField
    val ENTITY_REF = createRegistryKey<EntityRef>("entity_ref")

    @JvmField
    val ELEMENT = createRegistryKey<Element>("element")

    @JvmField
    val LEVEL_TO_RARITY_MAPPING = createRegistryKey<LevelToRarityMapping>("level_to_rarity_mapping")

    ///

    private fun <T> createRegistryKey(name: String): RegistryKey<out Registry<T>> {
        return RegistryKey.of(ROOT_REGISTRY_NAME, Identifiers.of(name))
    }
}