package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ecs.external.ComponentBridge
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap

data class WithAbility(
    /**
     * 该生物的技能, 键为技能的标识符 [cc.mewcraft.wakame.ability.Ability.key], 值为技能实体.
     */
    val abilities: Multimap<AbilityArchetype, Entity> = HashMultimap.create(),
) : Component<WithAbility> {

    fun abilityEntities(archetype: AbilityArchetype): Collection<ComponentBridge> {
        return abilities[archetype].map { ComponentBridge(it) }
    }

    companion object : ComponentType<WithAbility>()

    override fun type(): ComponentType<WithAbility> = WithAbility
}