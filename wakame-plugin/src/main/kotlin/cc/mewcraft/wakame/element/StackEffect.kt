package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required

@ConfigSerializable
data class StackEffect(
    @Required
    val maxAmount: Int,
    @Required
    val disappearTime: Int,
    @Required
    val stages: List<StackEffectStage>
)

@ConfigSerializable
data class StackEffectStage(
    @Required
    val amount: Int,
    @Required
    val abilities: List<RegistryEntry<AbilityMeta>>
)