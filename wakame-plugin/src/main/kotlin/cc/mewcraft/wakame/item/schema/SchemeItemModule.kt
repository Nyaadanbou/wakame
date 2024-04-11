package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.config.configurate.MaterialSerializer
import cc.mewcraft.wakame.config.configurate.PotionEffectSerializer
import cc.mewcraft.wakame.config.configurate.PotionEffectTypeSerializer
import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.item.EffectiveSlotSerializer
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCoreGroupSerializer
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCorePoolSerializer
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurseGroupSerializer
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCursePoolSerializer
import cc.mewcraft.wakame.item.schema.meta.*
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.reference.REFERENCE_SERIALIZERS
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val BASE_SERIALIZERS = "base_serializers"
const val CELL_SERIALIZERS = "cell_serializers"
const val META_SERIALIZERS = "meta_serializers"
const val SKILL_SERIALIZERS = "skill_serializers"

internal fun schemaItemModule(): Module = module {

    single<TypeSerializerCollection>(named(BASE_SERIALIZERS)) {
        TypeSerializerCollection.builder()

            .kregister(EffectiveSlotSerializer)
            .register(MaterialSerializer)
            .kregister(PotionEffectSerializer)
            .register(PotionEffectTypeSerializer)

            .build()
    }

    single<TypeSerializerCollection>(named(CELL_SERIALIZERS)) {
        TypeSerializerCollection.builder()

            // cores
            .kregister(SchemaCorePoolSerializer)
            .kregister(SchemaCoreGroupSerializer)

            // curses
            .kregister(SchemaCursePoolSerializer)
            .kregister(SchemaCurseGroupSerializer)

            // curse contents
            .registerAll(get<TypeSerializerCollection>(named(REFERENCE_SERIALIZERS)))

            .build()
    }

    single<TypeSerializerCollection>(named(META_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .registerAll(get(named(ELEMENT_SERIALIZERS)))
            .registerAll(get(named(KIZAMI_SERIALIZERS)))
            .registerAll(get(named(RARITY_SERIALIZERS)))

            .kregister(ElementPoolSerializer)
            .kregister(KizamiPoolSerializer)
            .kregister(KizamiGroupSerializer)

            .kregister(DisplayLoreMetaSerializer)
            .kregister(DisplayNameMetaSerializer)
            .kregister(DurabilityMetaSerializer)
            .kregister(ElementMetaSerializer)
            .kregister(FoodMetaSerializer)
            .kregister(KizamiMetaSerializer)
            .kregister(LevelMetaSerializer)
            .kregister(RarityMetaSerializer)
            .kregister(SkinMetaSerializer)
            .kregister(SkinOwnerMetaSerializer)

            .build()
    }

    single<TypeSerializerCollection>(named(SKILL_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .build()
    }
}