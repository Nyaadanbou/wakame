package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.molang.Expression
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required

@ConfigSerializable
data class AbilityObject(
    @Required
    val ability: AbilityMeta,
    val trigger: AbilityTrigger?,
    val variant: AbilityTriggerVariant,
    val manaCost: Expression,
)