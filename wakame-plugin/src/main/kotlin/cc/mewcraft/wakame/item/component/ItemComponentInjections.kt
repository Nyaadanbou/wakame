package cc.mewcraft.wakame.item.component

import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

internal object ItemComponentInjections : KoinComponent {
    val mini: MiniMessage by inject()
}