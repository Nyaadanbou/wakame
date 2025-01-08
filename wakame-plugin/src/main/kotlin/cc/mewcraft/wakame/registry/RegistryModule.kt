package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.ability.ABILITY_GROUP_SERIALIZERS
import cc.mewcraft.wakame.ability.AbilitySerializer
import cc.mewcraft.wakame.ability.trigger.ABILITY_TRIGGER_SERIALIZERS
import cc.mewcraft.wakame.adventure.ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS
import cc.mewcraft.wakame.item.ITEM_PROTO_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_EXTERNALS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.serialization.configurate.mapperfactory.ObjectMappers
import cc.mewcraft.wakame.skin.SKIN_SERIALIZERS
import cc.mewcraft.wakame.util.buildYamlLoader
import cc.mewcraft.wakame.util.createYamlLoader
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

const val ABILITY_CONFIG_FILE = "ability.yml"
const val ABILITY_PROTO_CONFIG_DIR = "ability"
const val ABILITY_PROTO_CONFIG_LOADER = "ability_global_config_loader"

const val CRATE_PROTO_CONFIG_DIR = "crates"

const val CRATE_PROTO_CONFIG_LOADER = "crate_prototype_config_loader"
const val ITEM_PROTO_CONFIG_DIR = "items"

const val ITEM_PROTO_CONFIG_LOADER = "item_prototype_config_loader"
const val LANG_PROTO_CONFIG_DIR = "lang"

const val LANG_PROTO_CONFIG_LOADER = "lang_prototype_config_loader"

const val ATTRIBUTE_GLOBAL_CONFIG_FILE = "attributes.yml"
const val ATTRIBUTE_GLOBAL_CONFIG_LOADER = "attribute_global_config_loader"

const val CATEGORY_GLOBAL_CONFIG_FILE = "categories.yml"
const val CATEGORY_GLOBAL_CONFIG_LOADER = "category_config_loader"

const val PROJECTILE_GLOBAL_CONFIG_FILE = "projectiles.yml"
const val PROJECTILE_GLOBAL_CONFIG_LOADER = "projectile_global_config_loader"

const val RARITY_GLOBAL_CONFIG_FILE = "rarities.yml"
const val RARITY_GLOBAL_CONFIG_LOADER = "rarity_global_config_loader"

const val LEVEL_GLOBAL_CONFIG_FILE = "levels.yml"
const val LEVEL_GLOBAL_CONFIG_LOADER = "level_global_config_loader"

const val SKIN_GLOBAL_CONFIG_FILE = "skins.yml"
const val SKIN_GLOBAL_CONFIG_LOADER = "skin_global_config_loader"

const val ENTITY_GLOBAL_CONFIG_FILE = "entities.yml"
const val ENTITY_GLOBAL_CONFIG_LOADER = "entity_global_config_loader"

internal fun registryModule(): Module = module {
    single<YamlConfigurationLoader>(named(SKIN_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(SKIN_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(SKIN_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader.Builder>(named(ITEM_PROTO_CONFIG_LOADER)) {
        buildYamlLoader {
            registerAnnotatedObjects(ObjectMappers.DEFAULT)
            registerAll(get<TypeSerializerCollection>(named(ITEM_PROTO_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader.Builder>(named(LANG_PROTO_CONFIG_LOADER)) {
        buildYamlLoader {
            registerAll(get<TypeSerializerCollection>(named(ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader.Builder>(named(ABILITY_PROTO_CONFIG_LOADER)) {
        buildYamlLoader {
            register(AbilitySerializer)
            registerAll(get(named(ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS)))
            registerAll(get(named(ABILITY_GROUP_SERIALIZERS)))
            registerAll(get(named(ABILITY_TRIGGER_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(RARITY_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(RARITY_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(LEVEL_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(LEVEL_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(RARITY_EXTERNALS)))
        }
    }
}