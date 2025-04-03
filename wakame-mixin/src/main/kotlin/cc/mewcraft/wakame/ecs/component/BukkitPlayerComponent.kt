package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

// FIXME #365: 改名为 BukkitPlayer
data class BukkitPlayerComponent(
    val bukkitPlayer: Player,
) : Component<BukkitPlayerComponent> {
    companion object : ComponentType<BukkitPlayerComponent>()

    override fun type(): ComponentType<BukkitPlayerComponent> = BukkitPlayerComponent

    operator fun invoke(): Player = bukkitPlayer
}