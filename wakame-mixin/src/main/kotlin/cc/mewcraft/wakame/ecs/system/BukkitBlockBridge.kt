package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.Families
import cc.mewcraft.wakame.ecs.component.BukkitBlockComponent
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

/**
 * 每 tick 自动移除已经失效的 BukkitBlock Entity.
 */
class BukkitBlockBridge : IteratingSystem(
    family = Families.BUKKIT_BLOCK
) {
    override fun onTickEntity(entity: Entity) {
        val bukkitBlock = entity[BukkitBlockComponent]
        if (!bukkitBlock.bukkitBlock.location.isChunkLoaded) {
            entity.remove()
        }
    }
}