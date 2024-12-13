package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.Result
import cc.mewcraft.wakame.ecs.external.ComponentMap
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World

data class ResultComponent(
    var result: Result,
) : Component<ResultComponent> {
    override fun type(): ComponentType<ResultComponent> = ResultComponent

    override fun World.onAdd(entity: Entity) {
        val componentMap = ComponentMap(this, entity)
        result.onEnable(componentMap)
    }

    override fun World.onRemove(entity: Entity) {
        val componentMap = ComponentMap(this, entity)
        result.onDisable(componentMap)
    }

    companion object : ComponentType<ResultComponent>()
}