package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.FullKey
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * This singleton holds the necessary objects to implement
 * the subclasses of [BinaryItemMeta].
 */
internal object Implementations : KoinComponent {
    private val MINI: MiniMessage by inject()
    private val LINE_KEY_FACTORY: ItemMetaLineKeyFactory by inject()

    fun mini(): MiniMessage {
        return MINI
    }

    fun getLineKey(itemMeta: BinaryItemMeta<*>): FullKey {
        return LINE_KEY_FACTORY.get(itemMeta)
    }
}