package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class StackEffect(
    val maxAmount: Int,
    val disappearTime: Int,
    val stages: List<StackEffectStage>
)

@ConfigSerializable
data class StackEffectStage(
    val amount: Int,
    val abilities: List<RegistryEntry<AbilityMeta>>
)