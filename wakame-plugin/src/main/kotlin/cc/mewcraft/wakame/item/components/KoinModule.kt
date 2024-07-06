package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.components.cells.componentCellsModule
import cc.mewcraft.wakame.item.components.crates.componentCratesModule
import cc.mewcraft.wakame.item.components.legacy.componentLegacyModule
import cc.mewcraft.wakame.item.components.tracks.componentTracksModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun componentsModule(): Module = module {
    includes(
        componentCellsModule(),
        componentCratesModule(),
        componentLegacyModule(),
        componentTracksModule(),
    )
}