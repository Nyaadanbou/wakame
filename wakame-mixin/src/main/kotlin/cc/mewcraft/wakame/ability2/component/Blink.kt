package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class Blink(
    var distance: Int,
    var teleportedMessages: AudienceMessageGroup,
) : Component<Blink> {
    companion object : ComponentType<Blink>()

    override fun type(): ComponentType<Blink> = Blink

    var isTeleported: Boolean = false
}