package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class AbilityArchetypeComponent(
    val archetype: AbilityArchetype
) : Component<AbilityArchetypeComponent> {
    companion object : ComponentType<AbilityArchetypeComponent>()

    override fun type(): ComponentType<AbilityArchetypeComponent> = AbilityArchetypeComponent
}
