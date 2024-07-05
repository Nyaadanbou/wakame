package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.wakame.item.components.cells.cores.cellCoreModule
import cc.mewcraft.wakame.item.components.cells.curses.cellCurseModule
import cc.mewcraft.wakame.item.components.cells.reforge.cellReforgeModule
import cc.mewcraft.wakame.item.components.cells.template.cellTemplateModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun componentCellModule(): Module = module {
    includes(
        cellCoreModule(),
        cellCurseModule(),
        cellReforgeModule(),
        cellTemplateModule()
    )
}