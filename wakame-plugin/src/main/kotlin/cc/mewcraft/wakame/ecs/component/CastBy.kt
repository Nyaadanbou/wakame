package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ability.character.Caster
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class CastBy(
    var caster: Caster
) : Component<CastBy> {
    override fun type(): ComponentType<CastBy> = CastBy

    companion object : ComponentType<CastBy>()
}