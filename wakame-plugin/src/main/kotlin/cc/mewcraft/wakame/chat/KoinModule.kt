package cc.mewcraft.wakame.chat

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun chatModule(): Module = module {
    singleOf(::ItemChatRenderer) bind Initializable::class
}