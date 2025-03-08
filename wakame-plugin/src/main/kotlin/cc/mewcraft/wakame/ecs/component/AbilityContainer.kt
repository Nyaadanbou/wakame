package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap

data class AbilityContainer(
    /**
     * 该生物的技能, 键为技能的标识符 [cc.mewcraft.wakame.ability.Ability.key], 值为技能实体.
     */
    private val abilities: Multimap<AbilityArchetype, FleksEntity> = HashMultimap.create(),
) : Component<AbilityContainer> {
    companion object : ComponentType<AbilityContainer>()

    override fun type(): ComponentType<AbilityContainer> = AbilityContainer

    val values: Collection<FleksEntity>
        get() = abilities.values()

    operator fun get(archetype: AbilityArchetype): Collection<FleksEntity> {
        return abilities.get(archetype)
    }

    operator fun set(archetype: AbilityArchetype, entity: FleksEntity): Boolean {
        return abilities.put(archetype, entity)
    }
}
