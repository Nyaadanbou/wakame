package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Blink(
    var distance: Int,
    var teleportedMessages: AudienceMessageGroup,
) : Component<Blink> {
    var isTeleported: Boolean = false

    companion object : ComponentType<Blink>()

    override fun type(): ComponentType<Blink> = Blink
}