package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.Reloadable
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.createBasicConfigurationLoader
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
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
        RendererConfiguration(createBasicConfigurationLoader(RENDERER_CONFIG_FILE), get(named(MINIMESSAGE_FULL)))
    } binds arrayOf(Initializable::class, Reloadable::class)

    // meta lookup
    single<LoreMetaLookup> {
        val config = get<RendererConfiguration>()
        LoreMetaLookupImpl(config.loreIndexLookup, config.loreMetaLookup)
    }

    // text stylizers
    single<TextStylizer> {
        val config = get<RendererConfiguration>()
        TextStylizerImpl(
            metaStylizer = MetaStylizerImpl(
                nameFormat = config.nameFormat,
                loreFormat = config.loreFormat,
                levelFormat = config.levelFormat,
                rarityFormat = config.rarityFormat,
                elementFormat = config.elementFormat,
                kizamiFormat = config.kizamiFormat,
                skinFormat = config.skinFormat,
                skinOwnerFormat = config.skinOwnerFormat,
            ),
            abilityStylizer = new(::AbilityStylizerImpl),
            attributeStylizer = AttributeStylizerImpl(
                attributeFormats = config.attributeFormats,
                attackSpeedFormat = config.attackSpeedFormat,
                operationStylizer = OperationStylizerImpl(config.operationFormat)
            ),
            metaLineKeys = new(::MetaLineKeySupplierImpl),
            abilityLineKeys = new(::AbilityLineKeySupplierImpl),
            attributeLineKeys = new(::AttributeLineKeySupplierImpl)
        )
    }

    // lore finalizer
    single<LoreFinalizer> {
        LoreFinalizerImpl(get<RendererConfiguration>().fixedLoreLines, get())
    }
}
