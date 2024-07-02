package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.wakame.item.components.cell.cores.cellCoreModule
import cc.mewcraft.wakame.item.components.cell.curses.cellCurseModule
import cc.mewcraft.wakame.item.components.cell.reforge.cellReforgeModule
import cc.mewcraft.wakame.item.components.cell.template.cellTemplateModule
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