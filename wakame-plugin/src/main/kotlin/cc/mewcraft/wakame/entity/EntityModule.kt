package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ENTITY_TYPE_HOLDER_EXTERNALS = "entity_type_holder_externals"
internal const val ENTITY_TYPE_HOLDER_SERIALIZER = "entity_type_holder_serializer"

internal fun entityModule(): Module = module {

    single<EntityKeyLookup> {
        fun registerImplementation(
            requiredPlugin: String,
            implementations: MutableList<EntityKeyLookupPart>,
            implementationCreator: () -> EntityKeyLookupPart,
        ) {
            if (get<WakamePlugin>().isPluginPresent(requiredPlugin)) {
                implementations.add(implementationCreator())
            }
        }

        EntityKeyLookupImpl(buildList {
//            registerImplementation("MythicMobs", this, ::MythicMobsEntityKeyLookup)
        })
    }

    // 外部代码使用
    single<TypeSerializerCollection>(named(ENTITY_TYPE_HOLDER_EXTERNALS)) {
        TypeSerializerCollection.builder()
            .kregister(EntityTypeHolderSerializer)
            .build()
    }

    single<TypeSerializerCollection>(named(ENTITY_TYPE_HOLDER_SERIALIZER)) {
        TypeSerializerCollection.builder()
            .kregister(EntityTypeHolderSerializer)
            .build()
    }

}
