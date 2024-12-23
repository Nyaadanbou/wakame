package cc.mewcraft.wakame.ecs.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import team.unnamed.mocha.MochaEngine

data class MochaEngineComponent(
    var mochaEngine: MochaEngine<*>,
) : Component<MochaEngineComponent> {
    override fun type(): ComponentType<MochaEngineComponent> = MochaEngineComponent

    companion object : ComponentType<MochaEngineComponent>()
}