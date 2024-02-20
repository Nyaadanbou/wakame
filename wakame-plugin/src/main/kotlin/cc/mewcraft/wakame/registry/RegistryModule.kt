package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.scheme.CELL_SERIALIZERS
import cc.mewcraft.wakame.item.scheme.META_SERIALIZERS
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.reference.REFERENCE_SERIALIZERS
import cc.mewcraft.wakame.skin.SKIN_SERIALIZERS
import cc.mewcraft.wakame.util.buildBasicConfigurationLoader
import cc.mewcraft.wakame.util.createBasicConfigurationLoader
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

const val CRATE_CONFIG_DIR = "crates"
const val CRATE_CONFIG_LOADER = "crate_config_loader"

const val ITEM_CONFIG_DIR = "items"
const val ITEM_CONFIG_LOADER = "item_config_loader"

const val SKILL_CONFIG_DIR = "skills"
const val SKILL_CONFIG_LOADER = "skill_config_loader"

const val ATTRIBUTE_CONFIG_FILE = "attributes.yml"
const val ATTRIBUTE_CONFIG_LOADER = "attribute_config_loader"

const val CATEGORY_CONFIG_FILE = "categories.yml"
const val CATEGORY_CONFIG_LOADER = "category_config_loader"

const val PROJECTILE_CONFIG_FILE = "projectiles.yml"
const val PROJECTILE_CONFIG_LOADER = "projectile_config_loader"

const val ELEMENT_CONFIG_FILE = "elements.yml"
const val ELEMENT_CONFIG_LOADER = "element_config_loader"

const val KIZAMI_CONFIG_FILE = "kizami.yml"
const val KIZAMI_CONFIG_LOADER = "kizami_config_loader"

const val RARITY_CONFIG_FILE = "rarities.yml"
const val RARITY_CONFIG_LOADER = "rarity_config_loader"

const val LEVEL_CONFIG_FILE = "levels.yml"
const val LEVEL_CONFIG_LOADER = "level_config_loader"

const val SKIN_CONFIG_FILE = "skins.yml"
const val SKIN_CONFIG_LOADER = "skin_config_loader"

const val ENTITY_CONFIG_FILE = "entities.yml"
const val ENTITY_CONFIG_LOADER = "entity_config_loader"

fun registryModule(): Module = module {

    // We need to explicitly declare these Initializable,
    // so the functions can be called by the Initializer
    val registryBindings = arrayOf(Initializable::class, Reloadable::class)
    single { AttributeRegistry } binds registryBindings
    single { ElementRegistry } binds registryBindings
    single { EntityReferenceRegistry } binds registryBindings
    single { ItemSkinRegistry } binds registryBindings
    single { KizamiRegistry } binds registryBindings
    single { NekoItemRegistry } binds registryBindings
    single { LevelMappingRegistry } binds registryBindings
    single { RarityRegistry } binds registryBindings

    //<editor-fold desc="Definitions of YamlConfigurationLoader">
    single<YamlConfigurationLoader>(named(ELEMENT_CONFIG_LOADER)) {
        createBasicConfigurationLoader(ELEMENT_CONFIG_FILE) {
            registerAll(get(named(ELEMENT_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(ENTITY_CONFIG_LOADER)) {
        createBasicConfigurationLoader(ENTITY_CONFIG_FILE) {
            registerAll(get(named(REFERENCE_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(SKIN_CONFIG_LOADER)) {
        createBasicConfigurationLoader(SKIN_CONFIG_FILE) {
            registerAll(get(named(SKIN_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)) {
        createBasicConfigurationLoader(KIZAMI_CONFIG_FILE) {
            registerAll(get(named(KIZAMI_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader.Builder>(named(ITEM_CONFIG_LOADER)) {
        buildBasicConfigurationLoader {
            registerAll(get<TypeSerializerCollection>(named(CELL_SERIALIZERS)))
            registerAll(get<TypeSerializerCollection>(named(META_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(RARITY_CONFIG_LOADER)) {
        createBasicConfigurationLoader(RARITY_CONFIG_FILE) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(LEVEL_CONFIG_LOADER)) {
        createBasicConfigurationLoader(LEVEL_CONFIG_FILE) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }
    //</editor-fold>

}