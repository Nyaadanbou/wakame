package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.rarity.RarityMappings
import cc.mewcraft.wakame.util.applyCommons
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader

private const val ELEMENTS_FILE_PATH = "elements.yml"
private const val ENTITY_REFERENCE_FILE_PATH = "entities.yml"
private const val ITEM_SKIN_FILE_PATH = "skins.yml"
private const val KIZAMI_FILE_PATH = "kizami.yml"
private const val RARITY_MAPPING_FILE_PATH = "levels.yml"
private const val RARITY_FILE_PATH = "rarity.yml"

fun registryModule(): Module = module {

    single(ElementRegistry.CONFIG_LOADER_QUALIFIER) {
        createRegistryConfigurationLoader(ELEMENTS_FILE_PATH) {
            register(Element::class.java, get())
        }
    }

    single(EntityRegistry.CONFIG_LOADER_QUALIFIER) {
        createRegistryConfigurationLoader(ENTITY_REFERENCE_FILE_PATH) {

        }
    }

    single(ItemSkinRegistry.CONFIG_LOADER_QUALIFIER) {
        createRegistryConfigurationLoader(ITEM_SKIN_FILE_PATH) {

        }
    }

    single(KizamiRegistry.CONFIG_LOADER_QUALIFIER) {
        createRegistryConfigurationLoader(KIZAMI_FILE_PATH) {
            register(Kizami::class.java, get())
        }
    }

    single(RarityMappingRegistry.CONFIG_LOADER_QUALIFIER) {
        createRegistryConfigurationLoader(RARITY_MAPPING_FILE_PATH) {
            register(RarityMappings::class.java, get())
        }
    }

    single(RarityRegistry.CONFIG_LOADER_QUALIFIER) {
        createRegistryConfigurationLoader(RARITY_FILE_PATH) {
            register(Rarity::class.java, get())
        }
    }

}

private fun Scope.getBundledFileAsBufferedReader(path: String): BufferedReader {
    return get<WakamePlugin>().getBundledFile(path).bufferedReader()
}

private fun Scope.createRegistryConfigurationLoader(path: String, builder: TypeSerializerCollection.Builder.() -> Unit): YamlConfigurationLoader {
    return YamlConfigurationLoader.builder()
        .applyCommons()
        .source { getBundledFileAsBufferedReader(path) }
        .defaultOptions { options ->
            options.serializers { builder ->
                builder.builder()
            }
        }.build()
}