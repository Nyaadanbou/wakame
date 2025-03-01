package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.Interaction
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

data class BukkitBridgeComponent(
    val bukkitEntity: Entity
) : Component<BukkitBridgeComponent> {
    fun player(): Player = bukkitEntity as Player
    fun living(): LivingEntity = bukkitEntity as LivingEntity
    fun display(): Display = bukkitEntity as Display
    fun interaction(): Interaction = bukkitEntity as Interaction

    companion object : ComponentType<BukkitBridgeComponent>()

    override fun type(): ComponentType<BukkitBridgeComponent> = BukkitBridgeComponent
}