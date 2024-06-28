package cc.mewcraft.wakame.item.component

import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

internal object ItemComponentInjections : KoinComponent {
    val logger: Logger by inject()
    val mini: MiniMessage by inject()
}