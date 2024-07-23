package cc.mewcraft.wakame.skill.factory

import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val SKILL_FACTORY_SERIALIZERS = "skill_factory_serializers"

internal fun skillFactoryModule(): Module = module {
    single<TypeSerializerCollection>(named(SKILL_FACTORY_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .register(ProjectileTriggerSerializer)
            .kregister(BloodrageEffectSerializer)
            .kregister(TeleportTypeSerializer)
            .build()
    }
}