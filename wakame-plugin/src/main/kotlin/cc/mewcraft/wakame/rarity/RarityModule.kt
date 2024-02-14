package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.util.registerKt
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val RARITY_SERIALIZERS = "rarity_serializers"

fun rarityModule(): Module = module {

    single<TypeSerializerCollection>(named(RARITY_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .registerKt(RaritySerializer())
            .registerKt(LevelMappingSerializer())
            .build()
    }

}
