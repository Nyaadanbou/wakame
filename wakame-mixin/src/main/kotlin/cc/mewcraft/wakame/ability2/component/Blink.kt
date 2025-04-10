package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class Blink(
    var distance: Int,
    var teleportedMessages: AudienceMessageGroup,
) : Component<Blink> {
    companion object : EComponentType<Blink>()

    override fun type(): EComponentType<Blink> = Blink

    var isTeleported: Boolean = false
}