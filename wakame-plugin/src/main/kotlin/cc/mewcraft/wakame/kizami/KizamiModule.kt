package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val KIZAMI_EXTERNALS = "kizami_externals"
internal const val KIZAMI_SERIALIZERS = "kizami_serializers"

internal fun kizamiModule(): Module = module {
    single<TypeSerializerCollection>(named(KIZAMI_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .registerAll(get(named(ELEMENT_SERIALIZERS)))
            .kregister(KizamiSerializer)
            .kregister(KizamiEffectSerializer)
            .kregister(KizamiInstanceSerializer)
            .build()
    }

    // 用于物品序列化
    single<TypeSerializerCollection>(named(KIZAMI_EXTERNALS)) {
        TypeSerializerCollection.builder()
            .kregister(KizamiSerializer)
            .build()
    }
}