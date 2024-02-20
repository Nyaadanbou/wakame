package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.createBasicConfigurationLoader
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

const val RENDERER_CONFIG_FILE = "renderer.yml"
const val RENDERER_CONFIG_LOADER = "renderer_config_loader"

fun displayModule(): Module = module {

    // non-internals
    singleOf(::ItemRendererImpl) bind ItemRenderer::class
    singleOf(::ItemRendererListener)

    // line index lookup
    single<LoreMetaLookup> {
        val loader = get<RendererConfiguration>()
        LoreMetaLookupImpl(loader.fullIndexLookup)
    }

    // line key suppliers
    singleOf(::AbilityLineKeySupplierImpl) bind AbilityLineKeySupplier::class
    singleOf(::AttributeLineKeySupplierImpl) bind AttributeLineKeySupplier::class
    singleOf(::MetaLineKeySupplierImpl) bind MetaLineKeySupplier::class

    // lore line finalizer
    single<LoreFinalizer> {
        val loader = get<RendererConfiguration>()
        LoreFinalizerImpl(get<LoreLineComparator>(), loader.fixedLoreLines)
    }

    // lore line comparator
    singleOf(::LoreLineComparator)

    // stylizers
    singleOf(::LoreStylizerImpl) bind LoreStylizer::class
    singleOf(::AbilityStylizerImpl) bind AbilityStylizer::class
    single<AttributeStylizer> {
        val loader = get<RendererConfiguration>()
        AttributeStylizerImpl(loader.attributeFormats, loader.attackSpeedFormat, get())
    }
    single<OperationStylizer> {
        val loader = get<RendererConfiguration>()
        OperationStylizerImpl(loader.operationFormat)
    }
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
    singleOf(::NameStylizerImpl) bind NameStylizer::class

    // configuration loader
    single<NekoConfigurationLoader>(named(RENDERER_CONFIG_LOADER)) {
        createBasicConfigurationLoader(RENDERER_CONFIG_FILE)
    }

    // configuration holder
    single<RendererConfiguration> {
        RendererConfiguration(get(named(RENDERER_CONFIG_LOADER)), get(named(MINIMESSAGE_FULL)))
    } binds arrayOf(Initializable::class, Reloadable::class)
}
