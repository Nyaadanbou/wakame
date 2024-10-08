package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.components.crates.cratesModule
import cc.mewcraft.wakame.item.components.legacy.legacyModule
import cc.mewcraft.wakame.item.components.tracks.tracksModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun componentsModule(): Module = module {
    includes(
        cratesModule(),
        legacyModule(),
        tracksModule(),
    )
}