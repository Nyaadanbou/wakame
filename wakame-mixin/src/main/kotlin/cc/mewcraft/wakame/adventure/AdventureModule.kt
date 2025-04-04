package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.adventure.text.adventureTextModule
import cc.mewcraft.wakame.adventure.translator.adventureTranslatorModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun adventureModule(): Module = module {
    includes(
        adventureTextModule(),
        adventureTranslatorModule()
    )
}