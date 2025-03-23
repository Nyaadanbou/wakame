package cc.mewcraft.wakame.adventure.translator

import org.koin.core.module.Module
import org.koin.dsl.module

fun adventureTranslatorModule(): Module = module {
    single<MiniMessageTranslator> { MiniMessageTranslatorImpl() }
}