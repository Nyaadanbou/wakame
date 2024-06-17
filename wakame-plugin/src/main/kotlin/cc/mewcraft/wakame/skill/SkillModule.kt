package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.molang.EvaluableSerializer
import cc.mewcraft.wakame.skill.condition.SkillConditionGroupSerializer
import cc.mewcraft.wakame.skill.factory.factoryModule
import cc.mewcraft.wakame.skill.state.PlayerSkillStateShower
import cc.mewcraft.wakame.skill.state.SkillStateShower
import cc.mewcraft.wakame.skill.trigger.ConfiguredSkillSerializer
import cc.mewcraft.wakame.skill.trigger.TriggerSerializer
import cc.mewcraft.wakame.skill.trigger.triggerModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val SKILL_SERIALIZERS = "skill_serializers"

internal fun skillModule(): Module = module {

    includes(triggerModule())
    includes(factoryModule())

    singleOf(::SkillEventHandler)
    singleOf(::PlayerSkillStateShower) bind SkillStateShower::class

    singleOf(::SkillCastManagerImpl) bind SkillCastManager::class

    single<TypeSerializerCollection>(named(SKILL_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(SkillConditionGroupSerializer)
            .kregister(EvaluableSerializer)
            .kregister(ConfiguredSkillSerializer)
            .kregister(TriggerSerializer)
            .kregister(SkillDisplaySerializer)
            .build()
    }
}