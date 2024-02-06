package cc.mewcraft.wakame.item.scheme

import cc.mewcraft.wakame.element.ELEMENT_SERIALIZERS
import cc.mewcraft.wakame.item.scheme.core.SchemeCoreGroupSerializer
import cc.mewcraft.wakame.item.scheme.core.SchemeCorePoolSerializer
import cc.mewcraft.wakame.item.scheme.curse.SchemeCurseGroupSerializer
import cc.mewcraft.wakame.item.scheme.curse.SchemeCursePoolSerializer
import cc.mewcraft.wakame.item.scheme.meta.*
import cc.mewcraft.wakame.kizami.KIZAMI_SERIALIZERS
import cc.mewcraft.wakame.rarity.RARITY_SERIALIZERS
import cc.mewcraft.wakame.util.typedRegister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val CELL_SERIALIZERS = "cell_serializers"
const val META_SERIALIZERS = "meta_serializers"

fun schemeItemModule(): Module = module {

    // region Cells
    single<TypeSerializerCollection>(named(CELL_SERIALIZERS)) {
        TypeSerializerCollection.builder().apply {
            // cores
            typedRegister(SchemeCorePoolSerializer())
            typedRegister(SchemeCoreGroupSerializer())

            // curses
            typedRegister(SchemeCursePoolSerializer())
            typedRegister(SchemeCurseGroupSerializer())
        }.build()
    }
    // endregion

    // region Metas
    single<TypeSerializerCollection>(named(META_SERIALIZERS)) {
        TypeSerializerCollection.builder().apply {
            registerAll(get(named(ELEMENT_SERIALIZERS)))
            registerAll(get(named(KIZAMI_SERIALIZERS)))
            registerAll(get(named(RARITY_SERIALIZERS)))

            typedRegister(ElementPoolSerializer())
            typedRegister(KizamiPoolSerializer())

            typedRegister(DisplayNameMetaSerializer())
            typedRegister(ElementMetaSerializer())
            typedRegister(KizamiMetaSerializer())
            typedRegister(LevelMetaSerializer())
            typedRegister(LoreMetaSerializer())
            typedRegister(MaterialMetaSerializer())
            typedRegister(RarityMetaSerializer())
            typedRegister(SkinMetaSerializer())
            typedRegister(SkinOwnerMetaSerializer())
        }.build()
    }
    // endregion

}