package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

/**
 * Represents something that can provide a `display name` for the object.
 *
 * The `display name` is essentially a single text [Component].
 */
interface DisplayNameProvider {
    /**
     * Provides a [Component] describes the `display name`.
     */
    fun provideDisplayName(): Component
}
