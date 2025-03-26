package cc.mewcraft.wakame.ability2.component

import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class MultiJump(
    var count: Int,
    var jumpedMessages: AudienceMessageGroup,
) : Component<MultiJump> {
    companion object : ComponentType<MultiJump>() {
        const val USE_COOLDOWN = 5L
    }

    override fun type(): ComponentType<MultiJump> = MultiJump

    val originCount: Int = count
    var cooldown: Long = USE_COOLDOWN
    var isHoldingJump: Boolean = false
    var jumpCount: Int = count
}
