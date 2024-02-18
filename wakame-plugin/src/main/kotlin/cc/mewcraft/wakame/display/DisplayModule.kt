package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.applyCommons
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.File

const val RENDERER_CONFIG_FILE = "renderer.yml"
const val RENDERER_CONFIG_LOADER = "renderer_config_loader"
const val RENDERER_SERIALIZERS = "renderer_serializers"
const val FIXED_LORE_LINES = "fixed_lore_lines"

fun displayModule(): Module = module {

    // non-internals
    singleOf(::ItemRendererImpl) bind ItemRenderer::class
    singleOf(::ItemRendererListener)

    // line index supplier
    single<LineIndexLookup> {
        val loader = get<RendererConfiguration>()
        LineIndexLookupImpl(loader.loreLineIndexes)
    }
    // line key suppliers
    singleOf(::AbilityLineKeySupplierImpl) bind AbilityLineKeySupplier::class
    singleOf(::AttributeLineKeySupplierImpl) bind AttributeLineKeySupplier::class
    singleOf(::MetaLineKeySupplierImpl) bind MetaLineKeySupplier::class

    // finalizer
    single<LoreLineFinalizer> {
        val loader = get<RendererConfiguration>()
        LoreLineFinalizerImpl(get<LoreLineComparator>(), loader.fixedLoreLines)
    }

    // lore line comparator
    singleOf(::LoreLineComparator)

    // stylizers
    singleOf(::LoreStylizerImpl) bind LoreStylizer::class
    singleOf(::AbilityStylizerImpl) bind AbilityStylizer::class
    single<AttributeStylizer> {
        val loader = get<RendererConfiguration>()
        AttributeStylizerImpl(loader.attributeFormats, get())
    }
    single<OperationStylizer> {
        val loader = get<RendererConfiguration>()
        OperationStylizerImpl(loader.operationFormat)
    }
    singleOf(::MetaStylizerImpl) bind MetaStylizer::class
    singleOf(::NameStylizerImpl) bind NameStylizer::class

    // configuration loader
    single<NekoConfigurationLoader>(named(RENDERER_CONFIG_LOADER)) {
        createRendererConfigurationLoader(RENDERER_CONFIG_FILE) {
            // registerAll(get(named(RENDERER_SERIALIZERS)))
        }
    }

    single<RendererConfiguration> { RendererConfiguration(get<NekoConfigurationLoader>(named(RENDERER_CONFIG_LOADER)), get()) }
    
    single<MetaStylizer> {
        val loader = get<RendererConfiguration>()

        MetaStylizerImpl(
            nameFormat = loader.nameFormat,
            loreFormat = loader.loreFormat,
            levelFormat = loader.levelFormat,
            rarityFormat = loader.rarityFormat,
            elementFormat = loader.elementFormat,
            kizamiFormat = loader.kizamiFormat,
            skinFormat = loader.skinFormat,
            skinOwnerFormat = loader.skinOwnerFormat,
        )
    }

}

private fun Scope.getConfigFileAsBufferedReader(path: String): BufferedReader {
    val plugin = getOrNull<WakamePlugin>()
    return if (plugin != null) {
        // we are in a server environment
        get<WakamePlugin>().getBundledFile(path).bufferedReader()
    } else {
        // we are in a testing environment
        get<File>(named(PLUGIN_DATA_DIR)).resolve(path).bufferedReader()
    }
}

private fun Scope.createRendererConfigurationLoader(
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