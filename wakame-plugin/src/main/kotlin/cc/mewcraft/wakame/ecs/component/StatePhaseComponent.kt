package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.data.StatePhase
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class StatePhaseComponent(
    var phase: StatePhase
) : Component<StatePhaseComponent> {
    override fun type(): ComponentType<StatePhaseComponent> = StatePhaseComponent

    companion object : ComponentType<StatePhaseComponent>()
}