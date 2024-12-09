package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.molang.EVALUABLE_SERIALIZERS
import cc.mewcraft.wakame.skill2.condition.SkillConditionGroupSerializer
import cc.mewcraft.wakame.skill2.display.SkillDisplaySerializer
import cc.mewcraft.wakame.skill2.state.display.EntityStateDisplay
import cc.mewcraft.wakame.skill2.state.display.StateDisplay
import cc.mewcraft.wakame.skill2.trigger.SkillTriggerSerializer
import cc.mewcraft.wakame.skill2.trigger.skillTriggerModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val SKILL_EXTERNALS = "skill_externals"
internal const val SKILL_GROUP_SERIALIZERS = "skill_group_serializers"

fun skill2Module(): Module = module {

    includes(
        skillTriggerModule(),
    )

    singleOf(::EntityStateDisplay) bind StateDisplay::class

    singleOf(::MechanicWorldInteraction)
    singleOf(::SkillListener)
    singleOf(::SkillEventHandler)

    // 用于外部代码
    single<TypeSerializerCollection>(named(SKILL_EXTERNALS)) {
        TypeSerializerCollection.builder()
            .kregister(SkillSerializer)
            .kregister(SkillTriggerSerializer)
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