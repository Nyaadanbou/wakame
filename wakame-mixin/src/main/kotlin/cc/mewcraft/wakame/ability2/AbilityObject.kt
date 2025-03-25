package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.molang.Expression
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class AbilityObject(
    @Required
    @Setting("id")
    val meta: AbilityMeta,
    val trigger: AbilityTrigger?,
    val variant: AbilityTriggerVariant,
    @Setting("mana_cost")
    val manaCost: Expression?,
)