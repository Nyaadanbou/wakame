package cc.mewcraft.wakame.skin

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val SKIN_SERIALIZERS = "skin_serializers"

fun skinModule(): Module = module {
    // TODO setup it

    single<TypeSerializerCollection>(named(SKIN_SERIALIZERS)) {
        TypeSerializerCollection.builder().apply {

        }.build()
    }

}