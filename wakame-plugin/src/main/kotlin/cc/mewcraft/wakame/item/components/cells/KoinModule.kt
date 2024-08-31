package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.wakame.item.components.cells.cores.cellsCoresModule
import cc.mewcraft.wakame.item.components.cells.reforge.cellsReforgeModule
import cc.mewcraft.wakame.item.components.cells.template.cellsTemplateModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun componentCellsModule(): Module = module {
    includes(
        cellsCoresModule(),
        cellsReforgeModule(),
        cellsTemplateModule()
    )
}