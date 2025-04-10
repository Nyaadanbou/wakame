package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import org.bukkit.entity.Player

class BukkitPlayer(
    delegate: Player,
) : Component<BukkitPlayer>, ObjectWrapper<Player>(delegate) {
    companion object : EComponentType<BukkitPlayer>()

    override fun type(): EComponentType<BukkitPlayer> = BukkitPlayer
}