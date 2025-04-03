package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.adventure.text.adventureTextModule
import cc.mewcraft.wakame.adventure.translator.adventureTranslatorModule
import cc.mewcraft.wakame.util.register
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS = "adventure_audience_message_serializers"

fun adventureModule(): Module = module {
    includes(
        adventureTextModule(),
        adventureTranslatorModule()
    )

    single<TypeSerializerCollection>(named(ADVENTURE_AUDIENCE_MESSAGE_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .register<AudienceMessage>(CombinedAudienceMessageSerializer)
            .register<AudienceMessageGroup>(AudienceMessageGroupSerializer)
            .build()
    }
}