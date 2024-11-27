package cc.mewcraft.wakame.skill2.system

import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.external.ExternalSupport
import cc.mewcraft.wakame.skill2.external.SkillComponentMap
import cc.mewcraft.wakame.util.Key
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import me.lucko.helper.metadata.Metadata
import me.lucko.helper.metadata.MetadataKey

class SkillBukkitEntityMetadataSystem : IteratingSystem(
    family = family { all(IdentifierComponent, BukkitEntityComponent) }
) {
    companion object {
        val COMPONENT_MAP_KEY: MetadataKey<SkillComponentMap> = MetadataKey.create("component_map", SkillComponentMap::class.java)
    }

    override fun onTickEntity(entity: Entity) {
        val bukkitEntity = entity[BukkitEntityComponent].entity
        val metadata = Metadata.provide(bukkitEntity)

        val componentMap = metadata.getOrNull(COMPONENT_MAP_KEY)
        if (componentMap != null) {
            entity.configure {
                it += componentMap.componentTable.values().map { it.internal() }
            }
            return
        }

        val newComponentMap = SkillComponentMap()
        for (factory in ExternalSupport.FACTORIES) {
            val skillKey = Key(entity[IdentifierComponent].id)
            val skill = SkillRegistry.INSTANCES[skillKey]
            newComponentMap.componentTable.put(skill, factory.externalKey, factory.createFromEntity(world, entity))
        }
        metadata.put(COMPONENT_MAP_KEY, newComponentMap)
    }
}