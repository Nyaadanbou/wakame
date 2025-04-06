package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity

class BukkitEntity(
    delegate: Entity,
) : Component<BukkitEntity>, ObjectWrapper<Entity>(delegate) {
    companion object : ComponentType<BukkitEntity>()

    override fun type(): ComponentType<BukkitEntity> = BukkitEntity
}