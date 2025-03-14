package cc.mewcraft.wakame.element

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Required
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class StackEffect(
    @Required
    @Setting(value = "max_amount")
    val maxAmount: Int,
    @Required
    @Setting(value = "disappear_time")
    val disappearTime: Int,
    @Required
    val stages: List<StackEffectStage>
)

@ConfigSerializable
data class StackEffectStage(
    @Required
    val amount: Int,
    @Required
    val abilities: List<RegistryEntry<Ability>>
)