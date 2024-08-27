package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

const val RENDERERS_CONFIG_DIR = "renderers/"

internal fun displayModule(): Module = module {
    // non-internals
    singleOf(::DynamicLoreMetaCreators)

    // internals
    single { RendererBootstrap } bind Initializable::class
}
