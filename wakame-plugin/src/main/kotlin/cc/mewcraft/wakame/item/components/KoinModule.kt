package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.components.cells.moduleCells
import cc.mewcraft.wakame.item.components.crates.moduleCrates
import cc.mewcraft.wakame.item.components.legacy.moduleLegacy
import cc.mewcraft.wakame.item.components.tracks.moduleTracks
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun componentsModule(): Module = module {
    includes(
        moduleCells(),
        moduleCrates(),
        moduleLegacy(),
        moduleTracks(),
    )
}