package cc.mewcraft.wakame.adventure.text

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun adventureTextModule(): Module = module {
    single<GsonComponentSerializer> { GsonComponentSerializer.gson() }
    single<MiniMessage> { MiniMessage.miniMessage() }
}