package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.item.scheme.core.SchemeCoreGroupSerializer
import cc.mewcraft.wakame.item.scheme.core.SchemeCorePoolSerializer
import cc.mewcraft.wakame.item.scheme.curse.SchemeCurseGroupSerializer
import cc.mewcraft.wakame.item.scheme.curse.SchemeCursePoolSerializer
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.reference.REFERENCE_SERIALIZERS
import cc.mewcraft.wakame.util.registerKt
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val CELL_SERIALIZERS = "cell_serializers"
const val META_SERIALIZERS = "meta_serializers"

internal fun schemeItemModule(): Module = module {

    // region Cells
    single<TypeSerializerCollection>(named(CELL_SERIALIZERS)) {
        TypeSerializerCollection.builder()

            // cores
            .registerKt(SchemeCorePoolSerializer())
            .registerKt(SchemeCoreGroupSerializer())

            // curses
            .registerKt(SchemeCursePoolSerializer())
            .registerKt(SchemeCurseGroupSerializer())

            // curse contents
            .registerAll(get<TypeSerializerCollection>(named(REFERENCE_SERIALIZERS)))

            .build()
    }
    // endregion

    // region Metas
    single<TypeSerializerCollection>(named(META_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .registerAll(get(named(ELEMENT_SERIALIZERS)))
            .registerAll(get(named(KIZAMI_SERIALIZERS)))
            .registerAll(get(named(RARITY_SERIALIZERS)))

            .registerKt(ElementPoolSerializer())
            .registerKt(KizamiPoolSerializer())

            .registerKt(DisplayNameMetaSerializer())
            .registerKt(ElementMetaSerializer())
            .registerKt(KizamiMetaSerializer())
            .registerKt(LevelMetaSerializer())
            .registerKt(LoreMetaSerializer())
            .registerKt(MaterialMetaSerializer())
            .registerKt(RarityMetaSerializer())
            .registerKt(SkinMetaSerializer())
            .registerKt(SkinOwnerMetaSerializer())

            .build()
    }
    // endregion

}