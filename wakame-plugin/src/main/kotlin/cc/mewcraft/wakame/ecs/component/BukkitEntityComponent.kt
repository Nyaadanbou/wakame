package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity as BukkitEntity

data class BukkitEntityComponent(
    var entity: BukkitEntity
) : Component<BukkitEntityComponent> {
    override fun type(): ComponentType<BukkitEntityComponent> = BukkitEntityComponent

    companion object : ComponentType<BukkitEntityComponent>()
}