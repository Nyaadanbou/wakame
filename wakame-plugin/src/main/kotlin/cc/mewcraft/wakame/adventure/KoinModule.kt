package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.adventure.component.adventureComponentModule
import cc.mewcraft.wakame.adventure.minimessage.adventureMiniMessageModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun adventureModule(): Module = module {
    includes(
        adventureComponentModule(),
        adventureMiniMessageModule(),
    )
}