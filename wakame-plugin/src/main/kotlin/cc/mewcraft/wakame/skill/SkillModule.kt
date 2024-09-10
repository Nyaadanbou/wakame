package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.molang.EVALUABLE_SERIALIZERS
import cc.mewcraft.wakame.skill.condition.SkillConditionGroupSerializer
import cc.mewcraft.wakame.skill.factory.skillFactoryModule
import cc.mewcraft.wakame.skill.state.display.PlayerStateDisplay
import cc.mewcraft.wakame.skill.state.display.StateDisplay
import cc.mewcraft.wakame.skill.tick.skillTickModule
import cc.mewcraft.wakame.skill.trigger.SkillTriggerSerializer
import cc.mewcraft.wakame.skill.trigger.skillTriggerModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val SKILL_EXTERNALS = "skill_externals"
internal const val SKILL_GROUP_SERIALIZERS = "skill_group_serializers"

internal fun skillModule(): Module = module {
    includes(
        skillFactoryModule(),
        skillTickModule(),
        skillTriggerModule(),
    )

    singleOf(::SkillEventHandler)
    singleOf(::PlayerStateDisplay) bind StateDisplay::class

    singleOf(::SkillCastManagerImpl) bind SkillCastManager::class

    // 用于外部代码
    single<TypeSerializerCollection>(named(SKILL_EXTERNALS)) {
        TypeSerializerCollection.builder()
            .kregister(SkillSerializer)
            .kregister(SkillTriggerSerializer)
            .kregister(ConfiguredSkillSerializer)
            .kregister(TriggerVariantSerializer)
            .build()
    }

    // 用于技能组本身
    single<TypeSerializerCollection>(named(SKILL_GROUP_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(SkillDisplaySerializer)
            .kregister(SkillConditionGroupSerializer)
            .registerAll(get(named(EVALUABLE_SERIALIZERS)))
            .build()
    }
}