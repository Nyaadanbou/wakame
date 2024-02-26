package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.createBasicConfigurationLoader
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

const val RENDERER_CONFIG_FILE = "renderer.yml"

internal fun displayModule(): Module = module {

    // non-internals
    singleOf(::ItemRendererImpl) bind ItemRenderer::class
    singleOf(::ItemRendererListener)

    // config holder
    single<RendererConfiguration> {
        RendererConfiguration(createBasicConfigurationLoader(RENDERER_CONFIG_FILE))
    } binds arrayOf(Initializable::class)

    // meta lookup
    single<LoreMetaLookup> {
        val config = get<RendererConfiguration>()
        LoreMetaLookupImpl(config.loreIndexLookup, config.loreMetaLookup)
    }

    // text stylizers
    single<TextStylizer> {
        TextStylizerImpl(
            metaStylizer = new(::MetaStylizerImpl),
            abilityStylizer = new(::AbilityStylizerImpl),
            attributeStylizer = AttributeStylizerImpl(get(), new(::OperationStylizerImpl)),
            metaLineKeys = new(::MetaLineKeySupplierImpl),
            abilityLineKeys = new(::AbilityLineKeySupplierImpl),
            attributeLineKeys = new(::AttributeLineKeySupplierImpl)
        )
    }

    // lore finalizer
    singleOf(::LoreFinalizerImpl) bind LoreFinalizer::class
}
