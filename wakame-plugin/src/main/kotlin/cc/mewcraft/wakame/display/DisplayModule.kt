package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module

const val RENDERER_GLOBAL_CONFIG_FILE = "renderer.yml"

internal fun displayModule(): Module = module {
    // non-internals
    singleOf(::PacketItemRenderer)
    singleOf(::DynamicLoreMetaCreatorRegistry)

    // config holder
    single {
        RendererConfig(Configs.YAML[RENDERER_GLOBAL_CONFIG_FILE])
    } withOptions {
        bind<Initializable>()
    }

    // meta lookup
    single<LoreMetaLookup> { get<RendererConfig>().let { LoreMetaLookupImpl(it.loreIndexLookup, it.loreMetaLookup) } }
}
