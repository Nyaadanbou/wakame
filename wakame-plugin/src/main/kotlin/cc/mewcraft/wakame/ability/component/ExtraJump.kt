package cc.mewcraft.wakame.ability.component

import cc.mewcraft.wakame.adventure.AudienceMessageGroup
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ExtraJump(
    var count: Int,
    var jumpedMessages: AudienceMessageGroup,
) : Component<ExtraJump> {
    val originCount: Int = count

    var cooldown: Long = USE_COOLDOWN
    var isHoldingJump: Boolean = false
    var jumpCount: Int = count

    companion object : ComponentType<ExtraJump>() {
        const val USE_COOLDOWN = 5L
    }

    override fun type(): ComponentType<ExtraJump> = ExtraJump
}
