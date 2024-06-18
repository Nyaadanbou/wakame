package cc.mewcraft.wakame.skill.condition

import cc.mewcraft.wakame.util.kregister
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

const val SKILL_CONDITION_SERIALIZERS = "skill_condition_serializers"

fun skillConditionModule() = module {
    single<TypeSerializerCollection>(named(SKILL_CONDITION_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(CombinedConditionMessageSerializer)
            .kregister(ConditionMessageGroupSerializer)
            .build()
    }
}