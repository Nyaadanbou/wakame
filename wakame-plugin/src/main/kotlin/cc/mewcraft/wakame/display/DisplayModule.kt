package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.kregister
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
        RendererConfiguration(Configs.get(RENDERER_CONFIG_FILE) {
            serializers {
                it.kregister(LoreFormatSerializer)
                it.kregister(ListFormatSerializer)
                it.kregister(AttributeFormatSerializer)
                it.kregister(AttackSpeedFormatSerializer)
                it.kregister(OperationFormatSerializer)
            }
        })
    } binds arrayOf(Initializable::class)

    // meta lookup
    single<LoreMetaLookup> {
        val config = get<RendererConfiguration>()
        LoreMetaLookupImpl(config.loreIndexLookup, config.loreMetaLookup)
    }

    // text stylizers
    single<TextStylizer> {
        TextStylizerImpl(
            itemMetaStylizer = new(::ItemMetaStylizerImpl),
            skillStylizer = new(::SkillStylizerImpl),
            attributeStylizer = AttributeStylizerImpl(get(), new(::OperationStylizerImpl)),
            itemMetaKeySupplier = new(::ItemMetaKeySupplierImpl),
            skillKeySupplier = new(::SkillKeySupplierImpl),
            attributeKeySupplier = new(::AttributeKeySupplierImpl)
        )
    }

    // lore finalizer
    singleOf(::LoreFinalizerImpl) bind LoreFinalizer::class
}
