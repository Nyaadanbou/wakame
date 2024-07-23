package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.adventure.text.adventureTextModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS = "adventure_audience_message_serializers"

internal fun adventureModule(): Module = module {
    includes(
        adventureTextModule(),
    )

    single<TypeSerializerCollection>(named(ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(CombinedAudienceMessageSerializer)
            .kregister(AudienceMessageGroupSerializer)
            .build()
    }
}