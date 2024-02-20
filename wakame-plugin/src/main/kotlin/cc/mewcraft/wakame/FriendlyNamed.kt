package cc.mewcraft.wakame

import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent

interface FriendlyNamed : KoinComponent {
    /**
     * The display name that end-users can understand.
     *
     * It's a MiniMessage string, so it should not be sent directly to
     * end-users.
     */
    val displayName: String

    /**
     * The display name that end-users can understand.
     *
     * It should be a [component][Component] deserialized from [displayName].
     */
    val displayNameComponent: Component
}