package cc.mewcraft.wakame.item.components.cell.template

import cc.mewcraft.wakame.item.components.cell.template.cores.templateCoresModule
import cc.mewcraft.wakame.item.components.cell.template.curses.templateCursesModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun cellTemplateModule(): Module = module {
    includes(
        templateCoresModule(),
        templateCursesModule()
    )
}