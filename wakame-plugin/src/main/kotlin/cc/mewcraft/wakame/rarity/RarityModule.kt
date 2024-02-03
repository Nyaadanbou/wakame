package cc.mewcraft.wakame.rarity

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun rarityModule(): Module = module {
    singleOf(::RaritySerializer)
    singleOf(::RarityMappingSerializer)
}
