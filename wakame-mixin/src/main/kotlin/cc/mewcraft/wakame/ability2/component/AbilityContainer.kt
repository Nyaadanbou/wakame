package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.AbilityObject
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.molang.Expression
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap

data class AbilityContainer(
    /**
     * 该生物的技能, 键为技能的标识符 [AbilityMeta], 值为技能实体.
     */
    private val abilities: Multimap<AbilityMeta, Entity> = HashMultimap.create(),
) : Component<AbilityContainer> {
    companion object : ComponentType<AbilityContainer>()

    override fun type(): ComponentType<AbilityContainer> = AbilityContainer

    fun convertToAbilityObjectList(): List<AbilityObject> {
        val abilityEntities = abilities.values().map(::KoishEntity)
        val playerAbilities = abilityEntities.map { koishEntity ->
            val abilityComponent = koishEntity[AbilityComponent]
            val manaCost = koishEntity.getOrNull(ManaCost)?.manaCost ?: Expression.of(0)
            AbilityObject(
                ability = abilityComponent.ability,
                trigger = abilityComponent.trigger,
                variant = abilityComponent.variant,
                manaCost = manaCost
            )
        }
        return playerAbilities
    }

    operator fun get(archetype: AbilityMeta): Collection<Entity> {
        return abilities.get(archetype)
    }

    operator fun set(archetype: AbilityMeta, entity: Entity): Boolean {
        return abilities.put(archetype, entity)
    }

    fun remove(archetype: AbilityMeta, entity: Entity) {
        abilities.remove(archetype, entity)
    }
}
