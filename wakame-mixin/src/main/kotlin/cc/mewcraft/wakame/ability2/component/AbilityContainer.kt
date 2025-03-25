package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.SingleAbility
import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.molang.Expression
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap

data class AbilityContainer(
    /**
     * 该生物的技能, 键为技能的标识符 [AbilityMetaType], 值为技能实体.
     */
    private val abilities: Multimap<AbilityMetaType<*>, Entity> = HashMultimap.create(),
) : Component<AbilityContainer> {
    companion object : ComponentType<AbilityContainer>()

    override fun type(): ComponentType<AbilityContainer> = AbilityContainer

    fun convertToSingleAbilityList(): List<SingleAbility> {
        val abilityEntities = abilities.values().map(::KoishEntity)
        val playerAbilities = abilityEntities.map { koishEntity ->
            val abilityComponent = koishEntity[AbilityComponent]
            val manaCost = koishEntity.getOrNull(ManaCost)?.manaCost ?: Expression.of(0)
            SingleAbility(
                metaType = abilityComponent.metaType,
                trigger = abilityComponent.trigger,
                variant = abilityComponent.variant,
                manaCost = manaCost
            )
        }
        return playerAbilities
    }

    operator fun get(metaType: AbilityMetaType<*>): Collection<Entity> {
        return abilities.get(metaType)
    }

    operator fun set(metaType: AbilityMetaType<*>, entity: Entity): Boolean {
        return abilities.put(metaType, entity)
    }

    fun remove(metaType: AbilityMetaType<*>, entity: Entity): Boolean {
        return abilities.remove(metaType, entity)
    }
}
