package cc.mewcraft.wakame.item.components.cells.template

import cc.mewcraft.wakame.item.components.cells.template.cores.templateCoresModule
import cc.mewcraft.wakame.item.components.cells.template.curses.templateCursesModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun cellTemplateModule(): Module = module {
    includes(
        templateCoresModule(),
        templateCursesModule()
    )
}