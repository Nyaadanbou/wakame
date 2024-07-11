package cc.mewcraft.wakame.rarity

import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val RARITY_EXTERNALS = "rarity_externals"
internal const val RARITY_SERIALIZERS = "rarity_serializers"

internal fun rarityModule(): Module = module {

    single<TypeSerializerCollection>(named(RARITY_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(GlowColorSerializer)
            .kregister(RaritySerializer)
            .kregister(LevelMappingSerializer)
            .build()
    }

    // 用于外部代码
    single<TypeSerializerCollection>(named(RARITY_EXTERNALS)) {
        TypeSerializerCollection.builder()
            .kregister(GlowColorSerializer)
            .kregister(RaritySerializer)
            .kregister(LevelMappingSerializer)
            .build()
    }

}
