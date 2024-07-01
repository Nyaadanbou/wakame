package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.wakame.item.components.cell.cores.coreModule
import cc.mewcraft.wakame.item.components.cell.curses.curseModule
import cc.mewcraft.wakame.item.components.cell.reforge.reforgeModule
import cc.mewcraft.wakame.item.components.cell.template.templateModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun cellModule(): Module = module {
    includes(
        coreModule(),
        curseModule(),
        reforgeModule(),
        templateModule()
    )
}