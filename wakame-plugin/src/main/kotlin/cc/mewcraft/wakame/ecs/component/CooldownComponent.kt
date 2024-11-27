package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.data.Cooldown
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class CooldownComponent(
    val cooldown: Cooldown = Cooldown(0f),
) : Component<CooldownComponent> {
    override fun type(): ComponentType<CooldownComponent> = CooldownComponent

    companion object : ComponentType<CooldownComponent>()
}