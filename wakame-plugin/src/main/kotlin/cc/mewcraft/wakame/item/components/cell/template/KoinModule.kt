package cc.mewcraft.wakame.item.components.cell.template

import cc.mewcraft.wakame.item.components.cell.template.cores.coresModule
import cc.mewcraft.wakame.item.components.cell.template.curses.cursesModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun templateModule(): Module = module {
    includes(
        coresModule(),
        cursesModule()
    )
}