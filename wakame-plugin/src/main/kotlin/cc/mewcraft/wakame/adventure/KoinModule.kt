package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.adventure.text.adventureTextModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun adventureModule(): Module = module {
    includes(
        adventureTextModule(),
    )
}