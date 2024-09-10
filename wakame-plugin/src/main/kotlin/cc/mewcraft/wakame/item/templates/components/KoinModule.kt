package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.templates.components.cells.moduleCells
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun moduleComponents(): Module = module {
    includes(
        moduleCells()
    )
}