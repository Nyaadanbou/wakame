package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.WakameWorld
import cc.mewcraft.wakame.ecs.component.BlockComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class BlockRemoveSystem : IteratingSystem(
    family = FamilyDefinitions.BLOCK
) {
    override fun onTickEntity(entity: Entity) {
        val block = entity[BlockComponent]
        if (!block.block.location.isChunkLoaded) {
            WakameWorld.removeEntity(entity)
        }
    }
}