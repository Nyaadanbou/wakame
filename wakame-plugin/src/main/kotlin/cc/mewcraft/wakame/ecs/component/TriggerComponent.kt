package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.skill2.trigger.Trigger
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TriggerComponent(
    var trigger: Trigger
) : Component<TriggerComponent> {
    override fun type(): ComponentType<TriggerComponent> = TriggerComponent

    companion object : ComponentType<TriggerComponent>()
}
