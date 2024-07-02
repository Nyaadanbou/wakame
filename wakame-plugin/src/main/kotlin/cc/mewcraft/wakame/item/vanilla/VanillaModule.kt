package cc.mewcraft.wakame.item.vanilla

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val VANILLA_COMPONENT_REMOVER_SERIALIZER = "vanilla_component_remover_serializer"

internal fun vanillaModule(): Module = module {
    single(named(VANILLA_COMPONENT_REMOVER_SERIALIZER)) {
        VanillaComponentRemoverSerializer
    }
}