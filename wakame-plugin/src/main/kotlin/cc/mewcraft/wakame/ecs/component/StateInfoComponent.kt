package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.skill2.state.StateInfo
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class StateInfoComponent(
    var stateInfo: StateInfo
) : Component<StateInfoComponent> {
    override fun type(): ComponentType<StateInfoComponent> = StateInfoComponent

    companion object : ComponentType<StateInfoComponent>()
}