package cc.mewcraft.wakame.skill2.external.component

import cc.mewcraft.wakame.ecs.component.TickCountComponent
import cc.mewcraft.wakame.skill2.external.ExternalComponent
import cc.mewcraft.wakame.skill2.external.ExternalComponentFactory
import cc.mewcraft.wakame.skill2.external.ExternalKey
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

interface TickCount : ExternalComponent<TickCountComponent> {

    var time: Double

    companion object : ExternalComponentFactory<TickCountComponent, TickCount> {
        override val externalKey: ExternalKey<TickCount> = ExternalKey("tick_count")

        override fun create(component: TickCountComponent): TickCount {
            return TickCountImpl(component)
        }

        override fun createFromEntity(world: World, entity: Entity): TickCount? {
            return with(world) { entity.getOrNull(TickCountComponent)?.let { create(it) } }
        }
    }
}

private class TickCountImpl(
    private val component: TickCountComponent
) : TickCount {
    override var time: Double
        get() = component.tick
        set(value) {
            component.tick = value
        }

    override fun internal(): TickCountComponent {
        return component
    }
}