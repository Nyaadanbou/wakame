package cc.mewcraft.wakame.ecs.system

import cc.mewcraft.wakame.ecs.CommonFamilies
import cc.mewcraft.wakame.ecs.component.BukkitBlock
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

/**
 * 每 tick 自动移除已经失效的 [BukkitBlock].
 */
object RemoveBukkitBlocks : IteratingSystem(
    family = CommonFamilies.BUKKIT_BLOCK
) {
    override fun onTickEntity(entity: Entity) {
        val bukkitBlock = entity[BukkitBlock]
        if (!bukkitBlock.unwrap().location.isChunkLoaded) {
            entity.remove()
        }
    }
}