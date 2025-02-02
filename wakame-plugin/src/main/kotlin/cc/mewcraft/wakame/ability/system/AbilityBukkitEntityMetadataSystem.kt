package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.CastBy
import cc.mewcraft.wakame.ecs.component.EntityType
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.util.metadata.Metadata
import cc.mewcraft.wakame.util.metadata.MetadataKey
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject

class AbilityBukkitEntityMetadataSystem(
    private val wakameWorld: WakameWorld = inject()
) : IteratingSystem(
    family = family { all(CastBy, EntityType.ABILITY) }
) {
    companion object {
        val COMPONENT_MAP_KEY: MetadataKey<ComponentMap> = MetadataKey.create("mechanic_component_map", ComponentMap::class.java)
    }

    override fun onTickEntity(entity: Entity) {
        val bukkitEntity = entity[CastBy].entity ?: return
        val metadata = Metadata.provide(bukkitEntity)
        val componentMap = wakameWorld.componentMap(entity)

        metadata.put(COMPONENT_MAP_KEY, componentMap)
    }
}