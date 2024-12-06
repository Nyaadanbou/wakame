package cc.mewcraft.wakame.skill2.system

import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.BukkitEntityComponent
import cc.mewcraft.wakame.ecs.external.ComponentMap
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import me.lucko.helper.metadata.Metadata
import me.lucko.helper.metadata.MetadataKey

class MechanicBukkitEntityMetadataSystem(
    private val wakameWorld: WakameWorld = inject()
) : IteratingSystem(
    family = family { all(BukkitEntityComponent) }
) {
    companion object {
        val COMPONENT_MAP_KEY: MetadataKey<ComponentMap> = MetadataKey.create("component_map", ComponentMap::class.java)
    }

    override fun onTickEntity(entity: Entity) {
        val bukkitEntity = entity[BukkitEntityComponent].entity ?: return
        val metadata = Metadata.provide(bukkitEntity)
        val componentMap = wakameWorld.componentMap(entity)

        metadata.put(COMPONENT_MAP_KEY, componentMap)
    }
}