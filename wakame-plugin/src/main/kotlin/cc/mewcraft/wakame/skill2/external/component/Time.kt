package cc.mewcraft.wakame.skill2.external.component

import cc.mewcraft.wakame.ecs.component.TimeComponent
import cc.mewcraft.wakame.skill2.external.ExternalComponent
import cc.mewcraft.wakame.skill2.external.ExternalComponentFactory
import cc.mewcraft.wakame.skill2.external.ExternalKey
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

interface Time : ExternalComponent<TimeComponent> {

    var time: Double

    companion object : ExternalComponentFactory<TimeComponent, Time> {
        override val externalKey: ExternalKey<Time> = ExternalKey("time")

        override fun create(component: TimeComponent): Time {
            return TimeImpl(component)
        }

        override fun createFromEntity(world: World, entity: Entity): Time? {
            return with(world) { entity.getOrNull(TimeComponent)?.let { create(it) } }
        }
    }
}

private class TimeImpl(
    private val component: TimeComponent
) : Time {
    override var time: Double
        get() = component.time
        set(value) {
            component.time = value
        }

    override fun internal(): TimeComponent {
        return component
    }
}