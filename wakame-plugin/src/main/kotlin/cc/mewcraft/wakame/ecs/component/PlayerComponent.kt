package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

data class PlayerComponent(
    val player: Player
) : Component<PlayerComponent> {
    companion object : ComponentType<PlayerComponent>()

    override fun type(): ComponentType<PlayerComponent> = PlayerComponent
}