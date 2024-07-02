package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ELEMENT_SERIALIZERS = "element_serializers"
internal const val ELEMENT_ITEM_PROTO_SERIALIZERS = "element_item_proto_serializers"

internal fun elementModule(): Module = module {
    single<TypeSerializerCollection>(named(ELEMENT_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister<Element>(ElementSerializer)
            .build()
    }

    // 用于物品序列化
    single<TypeSerializerCollection>(named(ELEMENT_ITEM_PROTO_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister<Element>(ElementSerializer)
            .build()
    }
}