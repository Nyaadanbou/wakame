package cc.mewcraft.wakame.item.schema

import cc.mewcraft.wakame.config.configurate.MaterialSerializer
import cc.mewcraft.wakame.config.configurate.PotionEffectSerializer
import cc.mewcraft.wakame.config.configurate.PotionEffectTypeSerializer
import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.entity.ENTITY_TYPE_HOLDER_SERIALIZER
import cc.mewcraft.wakame.item.EffectiveSlotSerializer
import cc.mewcraft.wakame.item.schema.cell.SchemaCellSerializer
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCoreGroupSerializer
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCorePoolSerializer
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurseGroupSerializer
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCursePoolSerializer
import cc.mewcraft.wakame.item.schema.meta.*
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.skill.SKILL_SERIALIZERS
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val BASE_SERIALIZERS = "base_serializers"
const val CELL_SERIALIZERS = "cell_serializers"
const val META_SERIALIZERS = "meta_serializers"

internal fun schemaItemModule(): Module = module {

    singleOf<NekoItemRealizer>(::NekoItemRealizerImpl)

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

            // cells
            .kregister(SchemaCellSerializer)

            // cores
            .kregister(SchemaCorePoolSerializer)
            .kregister(SchemaCoreGroupSerializer)
            .registerAll(get(named(SKILL_SERIALIZERS)))

            // curses
            .kregister(SchemaCursePoolSerializer)
            .kregister(SchemaCurseGroupSerializer)
            .registerAll(get(named(ENTITY_TYPE_HOLDER_SERIALIZER)))

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

            .kregister(DisplayNameMetaSerializer)
            .kregister(DurabilityMetaSerializer)
            .kregister(ElementMetaSerializer)
            .kregister(FoodMetaSerializer)
            .kregister(ItemNameMetaSerializer)
            .kregister(KizamiMetaSerializer)
            .kregister(LevelMetaSerializer)
            .kregister(LoreMetaSerializer)
            .kregister(RarityMetaSerializer)
            .kregister(SkinMetaSerializer)
            .kregister(SkinOwnerMetaSerializer)
            .kregister(ToolMetaSerializer)
            .kregister(ToolRuleSerializer)

            .build()
    }
}