package cc.mewcraft.wakame.reference

import cc.mewcraft.wakame.util.typedRegister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val REFERENCE_SERIALIZERS = "reference_serializers"

fun referenceModule(): Module = module {

    single<TypeSerializerCollection>(named(REFERENCE_SERIALIZERS)) {
        TypeSerializerCollection.builder().apply {
            typedRegister(EntityReferenceSerializer())
        }.build()
    }

}