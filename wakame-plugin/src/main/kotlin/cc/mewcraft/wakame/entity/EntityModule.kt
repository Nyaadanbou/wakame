package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val ENTITY_TYPE_HOLDER_SERIALIZER = "entity_type_holder_serializer"

internal fun entityModule(): Module = module {

    single<EntityKeyLookup> {
        val lookupList = mutableListOf<EntityKeyLookup>()

        // optionally add MM lookup
        val pl = get<WakamePlugin>()
        if (pl.isPluginPresent("MythicMobs")) {
            lookupList += MythicMobsEntityKeyLookup()
        }

        // always add vanilla lookup
        lookupList += VanillaEntityKeyLookup()

        CompositedEntityKeyLookup(lookupList)
    }

    single<TypeSerializerCollection>(named(ENTITY_TYPE_HOLDER_SERIALIZER)) {
        TypeSerializerCollection.builder()
            .kregister(EntityTypeHolderSerializer)
            .build()
    }

}