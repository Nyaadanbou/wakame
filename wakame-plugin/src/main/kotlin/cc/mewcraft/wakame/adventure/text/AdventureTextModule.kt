package cc.mewcraft.wakame.adventure.text

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun adventureTextModule(): Module = module {
    singleOf(GsonComponentSerializer::gson)
    singleOf(MiniMessage::miniMessage)
}