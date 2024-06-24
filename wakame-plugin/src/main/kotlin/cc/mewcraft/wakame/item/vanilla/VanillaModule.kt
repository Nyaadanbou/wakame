package cc.mewcraft.wakame.item.vanilla

import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val VANILLA_COMPONENT_REMOVER_SERIALIZER = "vanilla_component_remover_serializer"

internal fun vanillaModule(): Module = module {
    single<TypeSerializerCollection>(named(VANILLA_COMPONENT_REMOVER_SERIALIZER)) {
        TypeSerializerCollection.builder()
            .kregister(VanillaComponentRemoverSerializer)
            .build()
    }
}