package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

class BukkitPlayer(
    delegate: Player,
) : Component<BukkitPlayer>, ObjectWrapper<Player>(delegate) {
    companion object : ComponentType<BukkitPlayer>()

    override fun type(): ComponentType<BukkitPlayer> = BukkitPlayer
}