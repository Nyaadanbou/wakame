package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.util.typedRegister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val KIZAMI_SERIALIZERS = "kizami_serializers"

fun kizamiModule(): Module = module {

    single<TypeSerializerCollection>(named(KIZAMI_SERIALIZERS)) {
        TypeSerializerCollection.builder().apply {
            typedRegister(KizamiSerializer())
        }.build()
    }

}