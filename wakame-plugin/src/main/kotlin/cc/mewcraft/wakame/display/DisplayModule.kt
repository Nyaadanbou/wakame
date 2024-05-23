package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.ShowNekoStack
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val RENDERER_CONFIG_FILE = "renderer.yml"

const val PLAY_ITEM_RENDERER = "play_item_renderer"
const val SHOW_ITEM_RENDERER = "show_item_renderer"

internal fun displayModule(): Module = module {

    // non-internals
    single<DynamicLoreMetaCreatorRegistry> { DynamicLoreMetaCreatorRegistryImpl() }
    singleOf(::PlayItemRenderer) withOptions { named(PLAY_ITEM_RENDERER); bind<ItemRenderer<PlayNekoStack>>() }
    singleOf(::ShowItemRenderer) withOptions { named(SHOW_ITEM_RENDERER); bind<ItemRenderer<ShowNekoStack>>() }
    single<NetworkItemSerializeListener> { NetworkItemSerializeListener(get(named(PLAY_ITEM_RENDERER))) }

    // config holder
    single<RendererConfiguration> { RendererConfigurationImpl(Configs.YAML[RENDERER_CONFIG_FILE]) } withOptions { bind<Initializable>() }

    // meta lookup
    single<LoreMetaLookup> { get<RendererConfiguration>().let { LoreMetaLookupImpl(it.loreIndexLookup, it.loreMetaLookup) } }

    // text stylizers
    single<TextStylizer> { TextStylizerImpl() }

    // lore finalizer
    single<LoreFinalizer> { LoreFinalizerImpl(get(), get()) }
}
