package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val KIZAMI_SERIALIZERS = "kizami_serializers"
internal const val KIZAMI_ITEM_PROTO_SERIALIZERS = "kizami_item_proto_serializers"

internal fun kizamiModule(): Module = module {
    singleOf(::KizamiEventHandler)

    single<TypeSerializerCollection>(named(KIZAMI_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .registerAll(get(named(ELEMENT_SERIALIZERS)))
            .kregister(KizamiSerializer)
            .kregister(KizamiEffectSerializer)
            .kregister(KizamiInstanceSerializer)
            .build()
    }

    // 用于物品序列化
    single<TypeSerializerCollection>(named(KIZAMI_ITEM_PROTO_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(KizamiSerializer)
            .build()
    }
}