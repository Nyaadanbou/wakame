package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger

/**
 * 代表了在 Ecs 世界里的单个技能的信息, 方便内部获取.
 */
@ConsistentCopyVisibility
data class AbilityInfo internal constructor(
    val metaType: AbilityMetaType<*>,
    val trigger: AbilityTrigger?,
)
