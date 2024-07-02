package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_SERIALIZER
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.schema.BASE_SERIALIZERS
import cc.mewcraft.wakame.item.schema.CELL_SERIALIZERS
import cc.mewcraft.wakame.item.schema.META_SERIALIZERS
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.skill.SKILL_GROUP_SERIALIZERS
import cc.mewcraft.wakame.skill.condition.SKILL_CONDITION_SERIALIZERS
import cc.mewcraft.wakame.skill.factory.SKILL_FACTORY_SERIALIZERS
import cc.mewcraft.wakame.skill.trigger.SKILL_TRIGGER_SERIALIZERS
import cc.mewcraft.wakame.skin.SKIN_SERIALIZERS
import cc.mewcraft.wakame.util.buildYamlLoader
import cc.mewcraft.wakame.util.createYamlLoader
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

const val CRATE_PROTO_CONFIG_DIR = "crates"
const val CRATE_PROTO_CONFIG_LOADER = "crate_prototype_config_loader"

const val ITEM_PROTO_CONFIG_DIR = "items"
const val ITEM_PROTO_CONFIG_LOADER = "item_prototype_config_loader"

const val ITEM_GLOBAL_CONFIG_FILE = "items.yml"
const val ITEM_GLOBAL_CONFIG_LOADER = "item_global_config_loader"

const val SKILL_PROTO_CONFIG_DIR = "skills"
const val SKILL_PROTO_CONFIG_LOADER = "skill_global_config_loader"

const val ATTRIBUTE_GLOBAL_CONFIG_FILE = "attributes.yml"
const val ATTRIBUTE_GLOBAL_CONFIG_LOADER = "attribute_global_config_loader"

const val CATEGORY_GLOBAL_CONFIG_FILE = "categories.yml"
const val CATEGORY_GLOBAL_CONFIG_LOADER = "category_config_loader"

const val PROJECTILE_GLOBAL_CONFIG_FILE = "projectiles.yml"
const val PROJECTILE_GLOBAL_CONFIG_LOADER = "projectile_global_config_loader"

const val ELEMENT_GLOBAL_CONFIG_FILE = "elements.yml"
const val ELEMENT_GLOBAL_CONFIG_LOADER = "element_global_config_loader"

const val KIZAMI_GLOBAL_CONFIG_FILE = "kizami.yml"
const val KIZAMI_GLOBAL_CONFIG_LOADER = "kizami_global_config_loader"

const val RARITY_GLOBAL_CONFIG_FILE = "rarities.yml"
const val RARITY_GLOBAL_CONFIG_LOADER = "rarity_global_config_loader"

const val LEVEL_GLOBAL_CONFIG_FILE = "levels.yml"
const val LEVEL_GLOBAL_CONFIG_LOADER = "level_global_config_loader"

const val SKIN_GLOBAL_CONFIG_FILE = "skins.yml"
const val SKIN_GLOBAL_CONFIG_LOADER = "skin_global_config_loader"

const val ENTITY_GLOBAL_CONFIG_FILE = "entities.yml"
const val ENTITY_GLOBAL_CONFIG_LOADER = "entity_global_config_loader"

internal fun registryModule(): Module = module {

    // We need to explicitly declare these Initializable,
    // so the functions can be called by the Initializer
    single { AttributeRegistry } bind Initializable::class
    single { BehaviorRegistry } bind Initializable::class
    single { ElementRegistry } bind Initializable::class
    single { EntityRegistry } bind Initializable::class
    single { ItemMetaRegistry } bind Initializable::class
    single { ItemRegistry } bind Initializable::class
    single { ItemSkinRegistry } bind Initializable::class
    single { KizamiRegistry } bind Initializable::class
    single { LevelMappingRegistry } bind Initializable::class
    single { RarityRegistry } bind Initializable::class
    single { SkillRegistry } bind Initializable::class

    single<YamlConfigurationLoader>(named(ELEMENT_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(ELEMENT_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(ELEMENT_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(ENTITY_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(ENTITY_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(ENTITY_TYPE_HOLDER_SERIALIZER)))
        }
    }

    single<YamlConfigurationLoader>(named(SKIN_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(SKIN_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(SKIN_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(KIZAMI_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(KIZAMI_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(KIZAMI_SERIALIZERS)))
            registerAll(get(named(SKILL_GROUP_SERIALIZERS)))
            registerAll(get(named(SKILL_CONDITION_SERIALIZERS)))
            registerAll(get(named(SKILL_FACTORY_SERIALIZERS)))
            registerAll(get(named(SKILL_TRIGGER_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader.Builder>(named(ITEM_PROTO_CONFIG_LOADER)) {
        buildYamlLoader {
            registerAll(get<TypeSerializerCollection>(named(BASE_SERIALIZERS)))
            registerAll(get<TypeSerializerCollection>(named(CELL_SERIALIZERS)))
            registerAll(get<TypeSerializerCollection>(named(META_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader.Builder>(named(SKILL_PROTO_CONFIG_LOADER)) {
        buildYamlLoader {
            registerAll(get(named(BASE_SERIALIZERS)))
            registerAll(get(named(SKILL_GROUP_SERIALIZERS)))
            registerAll(get(named(SKILL_CONDITION_SERIALIZERS)))
            registerAll(get(named(SKILL_FACTORY_SERIALIZERS)))
            registerAll(get(named(SKILL_TRIGGER_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(RARITY_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(RARITY_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(LEVEL_GLOBAL_CONFIG_LOADER)) {
        createYamlLoader(LEVEL_GLOBAL_CONFIG_FILE) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }
}