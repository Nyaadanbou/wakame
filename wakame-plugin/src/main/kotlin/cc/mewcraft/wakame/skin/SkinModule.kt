package cc.mewcraft.wakame.skin

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val SKIN_SERIALIZERS = "skin_serializers"

internal fun skinModule(): Module = module {

    single<TypeSerializerCollection>(named(SKIN_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            // TODO set it up
            .build()
    }

}