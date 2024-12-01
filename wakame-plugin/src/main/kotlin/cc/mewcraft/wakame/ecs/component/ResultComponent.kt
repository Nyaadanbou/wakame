package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.Result
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ResultComponent(
    var result: Result,
) : Component<ResultComponent> {
    override fun type(): ComponentType<ResultComponent> = ResultComponent

    companion object : ComponentType<ResultComponent>()
}