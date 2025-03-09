package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity

data class BukkitEntityComponent(
    val bukkitEntity: Entity,
) : Component<BukkitEntityComponent> {
    companion object : ComponentType<BukkitEntityComponent>()

    override fun type(): ComponentType<BukkitEntityComponent> = BukkitEntityComponent
}