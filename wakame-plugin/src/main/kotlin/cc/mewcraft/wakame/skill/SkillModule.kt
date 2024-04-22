package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.molang.EvaluableSerializer
import cc.mewcraft.wakame.skill.condition.SkillConditionGroupSerializer
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val SKILL_SERIALIZERS = "skill_serializers"

internal fun skillModule(): Module = module {

    singleOf(::SkillEventHandler)

    single<TypeSerializerCollection>(named(SKILL_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(SkillConditionGroupSerializer)
            .kregister(EvaluableSerializer)
            .kregister(SkillInstanceSerializer) // TODO cell-overhaul: 等技能框架确定后移除
            .kregister(SkillTriggerSerializer) // TODO cell-overhaul: 等技能框架确定后移除
            .build()
    }
}