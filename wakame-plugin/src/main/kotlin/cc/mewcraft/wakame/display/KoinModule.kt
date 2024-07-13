package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

const val RENDERER_GLOBAL_CONFIG_FILE = "renderer.yml"

internal fun displayModule(): Module = module {
    // non-internals
    singleOf(::PacketItemRenderer)
    singleOf(::DynamicLoreMetaCreators)

    // internals
    single { RendererBootstrap } bind Initializable::class
    single<RendererConfig> { RendererConfigImpl(Configs.YAML[RENDERER_GLOBAL_CONFIG_FILE]) }
    single<LoreMetaLookup> { LoreMetaLookupImpl(get()) }
    singleOf(::LoreLineFlatter)
}
