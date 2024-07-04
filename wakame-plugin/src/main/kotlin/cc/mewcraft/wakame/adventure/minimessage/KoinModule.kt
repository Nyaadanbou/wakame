package cc.mewcraft.wakame.adventure.minimessage

import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun adventureMiniMessageModule(): Module = module {
    single<MiniMessage> { MiniMessage.miniMessage() }
}