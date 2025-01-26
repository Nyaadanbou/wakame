package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.display.AbilityDisplaySerializer
import cc.mewcraft.wakame.ability.state.display.PlayerStateDisplay
import cc.mewcraft.wakame.ability.state.display.StateDisplay
import cc.mewcraft.wakame.ability.trigger.AbilityTriggerSerializer
import cc.mewcraft.wakame.ability.trigger.abilityTriggerModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val ABILITY_EXTERNALS = "ability_externals"
internal const val ABILITY_GROUP_SERIALIZERS = "ability_group_serializers"

fun abilityModule(): Module = module {

    includes(
        abilityTriggerModule(),
    )

    singleOf(::PlayerStateDisplay) bind StateDisplay::class

    singleOf(::AbilityWorldInteraction)

    // 用于外部代码
    single<TypeSerializerCollection>(named(ABILITY_EXTERNALS)) {
        TypeSerializerCollection.builder()
            .kregister(AbilitySerializer)
            .kregister(AbilityTriggerSerializer)
            .kregister(TriggerVariantSerializer)
            .build()
    }

    // 用于技能组本身
    single<TypeSerializerCollection>(named(ABILITY_GROUP_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(AbilityDisplaySerializer)
            .build()
    }
}