package cc.mewcraft.wakame.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable

/**
 * Represents an object with a player-friendly name.
 */
interface PlayerFriendlyNamed {
    /**
     * The display name that players can understand.
     */
    val displayName: Component

    /**
     * The styles provided by this object.
     *
     * Note that the style has nothing to do with [displayName].
     * It solely serves the purpose of applying special styles
     * to other text [components][Component].
     */
    val displayStyles: Array<StyleBuilderApplicable>
}