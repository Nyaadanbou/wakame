package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.BukkitEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity

// FIXME #365: 改名为 BukkitEntity
data class BukkitEntityComponent(
    val bukkitEntity: Entity,
) : Component<BukkitEntityComponent> {
    companion object : ComponentType<BukkitEntityComponent>()

    override fun type(): ComponentType<BukkitEntityComponent> = BukkitEntityComponent

    operator fun invoke(): BukkitEntity = bukkitEntity
}