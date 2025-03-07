package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.FamilyDefinitions
import cc.mewcraft.wakame.ecs.component.BukkitBlockComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

class BukkitBlockBridge : IteratingSystem(
    family = FamilyDefinitions.BUKKIT_BLOCK
) {
    override fun onTickEntity(entity: Entity) {
        val bukkitBlock = entity[BukkitBlockComponent]
        if (!bukkitBlock.bukkitBlock.location.isChunkLoaded) {
            entity.remove()
        }
    }
}