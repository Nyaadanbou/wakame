package cc.mewcraft.wakame.display

import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The singleton holds the common properties and functions to
 * write the code in this package.
 */
internal object DisplaySupport : KoinComponent {
    private val MINI: MiniMessage by inject()

    fun mini(): MiniMessage {
        return MINI
    }
}