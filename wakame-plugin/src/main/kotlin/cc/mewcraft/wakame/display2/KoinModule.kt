package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal const val RENDERERS_CONFIG_DIR = "renderers"

internal fun display2Module(): Module = module {
    single { ItemRenderers.SIMPLE } bind Initializable::class
    single { ItemRenderers.STANDARD } bind Initializable::class
    single { ItemRenderers.CRAFTING_STATION } bind Initializable::class
    single { ItemRenderers.MERGING_TABLE } bind Initializable::class
    single { ItemRenderers.MODDING_TABLE } bind Initializable::class
    single { ItemRenderers.RECYCLING_STATION } bind Initializable::class
    single { ItemRenderers.REROLLING_TABLE } bind Initializable::class
}