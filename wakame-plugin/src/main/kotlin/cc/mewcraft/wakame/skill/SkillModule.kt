package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.molang.EVALUABLE_SERIALIZERS
import cc.mewcraft.wakame.skill.condition.SkillConditionGroupSerializer
import cc.mewcraft.wakame.skill.condition.skillConditionModule
import cc.mewcraft.wakame.skill.factory.skillFactoryModule
import cc.mewcraft.wakame.skill.state.PlayerSkillStateShower
import cc.mewcraft.wakame.skill.state.SkillStateShower
import cc.mewcraft.wakame.skill.trigger.SkillTriggerSerializer
import cc.mewcraft.wakame.skill.trigger.skillTriggerModule
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val SKILL_GROUP_SERIALIZERS = "skill_group_serializers"
internal const val SKILL_ITEM_PROTO_SERIALIZERS = "skill_item_proto_serializers"

internal fun skillModule(): Module = module {
    includes(
        skillFactoryModule(),
        skillTriggerModule(),
        skillConditionModule(),
    )

    singleOf(::SkillEventHandler)
    singleOf(::SkillTickerListener)
    singleOf(::PlayerSkillStateShower) bind SkillStateShower::class

    singleOf(::SkillCastManagerImpl) bind SkillCastManager::class

    // 用于物品序列化
    single<TypeSerializerCollection>(named(SKILL_ITEM_PROTO_SERIALIZERS)) {
        TypeSerializerCollection.builder()
            .kregister(SkillTriggerSerializer)
            .kregister(ConfiguredSkillSerializer)
            .kregister(ConfiguredSkillVariantSerializer)
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