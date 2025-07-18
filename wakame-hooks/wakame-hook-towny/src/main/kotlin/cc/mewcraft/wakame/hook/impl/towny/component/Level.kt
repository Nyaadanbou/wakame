package cc.mewcraft.wakame.hook.impl.towny.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Level(
    var level: Int,
): Component<Level> {
    companion object : ComponentType<Level>()

    override fun type(): ComponentType<Level> = Level
}