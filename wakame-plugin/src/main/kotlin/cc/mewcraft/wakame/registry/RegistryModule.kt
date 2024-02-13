package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.scheme.CELL_SERIALIZERS
import cc.mewcraft.wakame.item.scheme.META_SERIALIZERS
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.reference.REFERENCE_SERIALIZERS
import cc.mewcraft.wakame.skin.SKIN_SERIALIZERS
import cc.mewcraft.wakame.util.applyCommons
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.binds
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.File

const val ITEM_CONFIG_DIR = "items"
const val ITEM_CONFIG_LOADER = "item_config_loader"

const val ELEMENT_CONFIG_FILE = "elements.yml"
const val ELEMENT_CONFIG_LOADER = "element_config_loader"

const val KIZAMI_CONFIG_FILE = "kizami.yml"
const val KIZAMI_CONFIG_LOADER = "kizami_config_loader"

const val RARITY_CONFIG_FILE = "rarities.yml"
const val RARITY_CONFIG_LOADER = "rarity_config_loader"

const val RARITY_MAPPING_CONFIG_FILE = "levels.yml"
const val RARITY_MAPPING_CONFIG_LOADER = "rarity_mapping_config_loader"

const val SKIN_CONFIG_FILE = "skins.yml"
const val SKIN_CONFIG_LOADER = "skin_config_loader"

const val ENTITY_CONFIG_FILE = "entities.yml"
const val ENTITY_CONFIG_LOADER = "entity_config_loader"

fun registryModule(): Module = module {

    // We need to explicitly declare these Initializable,
    // so the functions can be called by the Initializer
    val registryBindings = arrayOf(Initializable::class, Reloadable::class)
    single { ElementRegistry } binds registryBindings
    single { EntityRegistry } binds registryBindings
    single { ItemSkinRegistry } binds registryBindings
    single { KizamiRegistry } binds registryBindings
    single { NekoItemRegistry } binds registryBindings
    single { RarityMappingRegistry } binds registryBindings
    single { RarityRegistry } binds registryBindings

    //<editor-fold desc="Definitions of YamlConfigurationLoader">
    single<YamlConfigurationLoader>(named(ELEMENT_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(ELEMENT_CONFIG_FILE) {
            registerAll(get(named(ELEMENT_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(ENTITY_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(ENTITY_CONFIG_FILE) {
            registerAll(get(named(REFERENCE_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(SKIN_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(SKIN_CONFIG_FILE) {
            registerAll(get(named(SKIN_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(KIZAMI_CONFIG_FILE) {
            registerAll(get(named(KIZAMI_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader.Builder>(named(ITEM_CONFIG_LOADER)) {
        YamlConfigurationLoader.builder()
            .applyCommons()
            .defaultOptions { options ->
                options.serializers {
                    it.registerAll(get<TypeSerializerCollection>(named(CELL_SERIALIZERS)))
                    it.registerAll(get<TypeSerializerCollection>(named(META_SERIALIZERS)))
                }
            }
    }

    single<YamlConfigurationLoader>(named(RARITY_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(RARITY_CONFIG_FILE) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(RARITY_MAPPING_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(RARITY_MAPPING_CONFIG_FILE) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }
    //</editor-fold>

}

private fun Scope.getConfigFileAsBufferedReader(path: String): BufferedReader {
    val plugin = getOrNull<WakamePlugin>()
    return if (plugin != null) {
        // we are in a real server environment
        get<WakamePlugin>().getBundledFile(path).bufferedReader()
    } else {
        // we are in a testing environment
        get<File>(named(PLUGIN_DATA_DIR)).resolve(path).bufferedReader()
    }
}

private fun Scope.createRegistryConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit,
): YamlConfigurationLoader {
    return YamlConfigurationLoader.builder()
        .applyCommons()
        .source { getConfigFileAsBufferedReader(path) }
        .defaultOptions { options ->
            options.serializers { it.builder() }
        }
        .build()
}