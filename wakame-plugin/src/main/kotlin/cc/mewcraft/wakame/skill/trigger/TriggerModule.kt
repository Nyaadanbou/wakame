package cc.mewcraft.wakame.skill.trigger

import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val SKILL_TRIGGER_SERIALIZERS = "skill_trigger_serializers"

internal fun triggerModule(): Module = module {
    single<TypeSerializerCollection>(named(SKILL_TRIGGER_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .register(ConfiguredSkill.VariantSerializer)
            .kregister(ConfiguredSkillSerializer)
            .kregister(TriggerSerializer)
            .build()
    }
}