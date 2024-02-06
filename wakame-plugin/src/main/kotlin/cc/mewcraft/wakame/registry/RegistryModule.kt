package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
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
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader

const val ELEMENT_CONFIG_LOADER = "element_config_loader"
const val ENTITY_CONFIG_LOADER = "entity_config_loader"
const val SKIN_CONFIG_LOADER = "skin_config_loader"
const val KIZAMI_CONFIG_LOADER = "kizami_config_loader"
const val ITEM_CONFIG_LOADER = "item_config_loader"
const val RARITY_CONFIG_LOADER = "rarity_config_loader"

const val ELEMENT_CONFIG_PATH = "elements.yml"
const val ENTITY_REFERENCE_CONFIG_PATH = "entities.yml"
const val SKIN_CONFIG_PATH = "skins.yml"
const val KIZAMI_CONFIG_PATH = "kizami.yml"
const val ITEM_CONFIG_DIR = "items"
const val RARITY_CONFIG_PATH = "rarity.yml"

fun registryModule(): Module = module {

    single<YamlConfigurationLoader>(named(ELEMENT_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(ELEMENT_CONFIG_PATH) {
            registerAll(get(named(ELEMENT_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(ENTITY_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(ENTITY_REFERENCE_CONFIG_PATH) {
            registerAll(get(named(REFERENCE_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(SKIN_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(SKIN_CONFIG_PATH) {
            registerAll(get(named(SKIN_SERIALIZERS)))
        }
    }

    single<YamlConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(KIZAMI_CONFIG_PATH) {
            registerAll(get(named(KIZAMI_SERIALIZERS)))
        }
    }

    factory<YamlConfigurationLoader.Builder>(named(ITEM_CONFIG_LOADER)) {
        YamlConfigurationLoader.builder()
            .applyCommons()
            .defaultOptions { options ->
                options.serializers {
                    // cell serializers
                    it.registerAll(get<TypeSerializerCollection>(named(CELL_SERIALIZERS)))
                    // meta serializers
                    it.registerAll(get<TypeSerializerCollection>(named(META_SERIALIZERS)))
                }
            }
    }

    single<YamlConfigurationLoader>(named(RARITY_CONFIG_LOADER)) {
        createRegistryConfigurationLoader(RARITY_CONFIG_PATH) {
            registerAll(get(named(RARITY_SERIALIZERS)))
        }
    }

}

private fun Scope.getBundledFileAsBufferedReader(path: String): BufferedReader {
    return get<WakamePlugin>().getBundledFile(path).bufferedReader()
}

private fun Scope.createRegistryConfigurationLoader(
    path: String,
    builder: TypeSerializerCollection.Builder.() -> Unit,
): YamlConfigurationLoader {
    return YamlConfigurationLoader
        .builder()
        .applyCommons()
        .source { getBundledFileAsBufferedReader(path) }
        .defaultOptions { options ->
            options.serializers { it.builder() }
        }
        .build()
}