package cc.mewcraft.wakame.skill2.trigger

import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val SKILL_TRIGGER_SERIALIZERS = "skill_trigger_serializers"

internal fun skillTriggerModule(): Module = module {
    single<TypeSerializerCollection>(named(SKILL_TRIGGER_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(SkillTriggerSerializer)
            .build()
    }
}