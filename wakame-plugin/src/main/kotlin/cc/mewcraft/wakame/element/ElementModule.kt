package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.util.typedRegister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val ELEMENT_SERIALIZERS = "element_serializers"

fun elementModule(): Module = module {

    single<TypeSerializerCollection>(named(ELEMENT_SERIALIZERS)) {
        TypeSerializerCollection.builder().apply {
            typedRegister<Element>(ElementSerializer())
        }.build()
    }

}