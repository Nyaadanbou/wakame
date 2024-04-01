package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.configurate.MaterialSerializer
import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.item.EffectiveSlotSerializer
import cc.mewcraft.wakame.item.schema.core.SchemaCoreGroupSerializer
import cc.mewcraft.wakame.item.schema.core.SchemaCorePoolSerializer
import cc.mewcraft.wakame.item.schema.curse.SchemaCurseGroupSerializer
import cc.mewcraft.wakame.item.schema.curse.SchemaCursePoolSerializer
import cc.mewcraft.wakame.item.schema.meta.*
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.reference.REFERENCE_SERIALIZERS
import cc.mewcraft.wakame.util.registerKt
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val BASE_SERIALIZERS = "base_serializers"
const val CELL_SERIALIZERS = "cell_serializers"
const val META_SERIALIZERS = "meta_serializers"

internal fun schemaItemModule(): Module = module {

    single<TypeSerializerCollection>(named(BASE_SERIALIZERS)) {
        TypeSerializerCollection.builder()

            .registerKt(MaterialSerializer)
            .registerKt(EffectiveSlotSerializer)

            .build()
    }

    single<TypeSerializerCollection>(named(CELL_SERIALIZERS)) {
        TypeSerializerCollection.builder()

            // cores
            .registerKt(SchemaCorePoolSerializer)
            .registerKt(SchemaCoreGroupSerializer)

            // curses
            .registerKt(SchemaCursePoolSerializer)
            .registerKt(SchemaCurseGroupSerializer)

            // curse contents
            .registerAll(get<TypeSerializerCollection>(named(REFERENCE_SERIALIZERS)))

            .build()
    }

    single<TypeSerializerCollection>(named(META_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .registerAll(get(named(ELEMENT_SERIALIZERS)))
            .registerAll(get(named(KIZAMI_SERIALIZERS)))
            .registerAll(get(named(RARITY_SERIALIZERS)))

            .registerKt(ElementPoolSerializer)
            .registerKt(KizamiPoolSerializer)
            .registerKt(KizamiGroupSerializer)

            .registerKt(DisplayLoreMetaSerializer)
            .registerKt(DisplayNameMetaSerializer)
            .registerKt(DurabilityMetaSerializer)
            .registerKt(ElementMetaSerializer)
            .registerKt(KizamiMetaSerializer)
            .registerKt(LevelMetaSerializer)
            .registerKt(RarityMetaSerializer)
            .registerKt(SkinMetaSerializer)
            .registerKt(SkinOwnerMetaSerializer)

            .build()
    }

}