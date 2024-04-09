package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val KIZAMI_SERIALIZERS = "kizami_serializers"

internal fun kizamiModule(): Module = module {

    single<TypeSerializerCollection>(named(KIZAMI_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .registerAll(get(named(ELEMENT_SERIALIZERS)))
            .kregister(KizamiSerializer)
            .kregister(KizamiEffectSerializer)
            .kregister(KizamiInstanceSerializer)
            .build()
    }

}