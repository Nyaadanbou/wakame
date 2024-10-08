package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.templates.components.cells.cellsModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun componentsModule(): Module = module {
    includes(
        cellsModule()
    )
}