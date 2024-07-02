package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val EVALUABLE_SERIALIZERS = "evaluable_serializers"

internal fun molangModule(): Module = module {
    single<TypeSerializerCollection>(named(EVALUABLE_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(EvaluableSerializer)
            .build()
    }
}