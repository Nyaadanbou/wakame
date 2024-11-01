package cc.mewcraft.wakame

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable

interface FriendlyNamed {
    /**
     * The display name that end-users can understand.
     */
    val displayName: Component

    /**
     * The styles provided by this object.
     *
     * Note that the style has nothing to do with [displayName].
     * It solely serves the purpose of applying special styles
     * to other components.
     */
    val styles: Array<StyleBuilderApplicable>
}