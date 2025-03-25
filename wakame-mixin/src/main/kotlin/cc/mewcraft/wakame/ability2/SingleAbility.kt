package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.molang.Expression

data class SingleAbility(
    val metaType: AbilityMetaType<*>,
    val trigger: AbilityTrigger?,
    val variant: AbilityTriggerVariant,
    val manaCost: Expression?,
)
