package cc.mewcraft.wakame.adventure.component

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun adventureComponentModule(): Module = module {
    single<GsonComponentSerializer> { GsonComponentSerializer.gson() }
}