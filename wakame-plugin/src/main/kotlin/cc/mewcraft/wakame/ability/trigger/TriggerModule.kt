package cc.mewcraft.wakame.ability.trigger

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val ABILITY_TRIGGER_SERIALIZERS = "ability_trigger_serializers"

internal fun abilityTriggerModule(): Module = module {
    single<TypeSerializerCollection>(named(ABILITY_TRIGGER_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .register(AbilityTriggerSerializer)
            .build()
    }
}