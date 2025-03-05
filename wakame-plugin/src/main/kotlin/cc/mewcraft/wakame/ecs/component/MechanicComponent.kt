package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.external.KoishEntity
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

data class MechanicComponent(
    var mechanic: Mechanic,
) : Component<MechanicComponent> {
    override fun type(): ComponentType<MechanicComponent> = MechanicComponent

    override fun World.onAdd(entity: Entity) {
        val koishEntity = KoishEntity(entity)
        mechanic.onInit(koishEntity)
    }

    override fun World.onRemove(entity: Entity) {
        val koishEntity = KoishEntity(entity)
        mechanic.onDispose(koishEntity)
    }

    companion object : ComponentType<MechanicComponent>()
}