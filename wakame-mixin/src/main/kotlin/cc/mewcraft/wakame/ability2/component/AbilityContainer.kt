package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.ability2.meta.AbilityMetaType
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap

data class AbilityContainer(
    /**
     * 该生物的技能, 键为技能的标识符 [AbilityMetaType], 值为技能实体.
     */
    private val abilities: Multimap<AbilityMetaType<*>, Entity> = HashMultimap.create(),
) : Component<AbilityContainer> {
    companion object : EComponentType<AbilityContainer>()

    override fun type(): EComponentType<AbilityContainer> = AbilityContainer

    fun convertToSingleAbilityList(): List<Ability> {
        val abilityEntities = abilities.values().map(::KoishEntity)
        return abilityEntities.map { it[Ability] }
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
