package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.Injector
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.Server
import org.koin.core.component.get
import java.util.UUID
import org.bukkit.entity.Entity as BukkitEntity

data class BukkitEntityComponent(
    var entityUniqueId: UUID
) : Component<BukkitEntityComponent> {

    val entity: BukkitEntity?
        get() = Injector.get<Server>().getEntity(entityUniqueId)

    override fun type(): ComponentType<BukkitEntityComponent> = BukkitEntityComponent

    companion object : ComponentType<BukkitEntityComponent>()
}