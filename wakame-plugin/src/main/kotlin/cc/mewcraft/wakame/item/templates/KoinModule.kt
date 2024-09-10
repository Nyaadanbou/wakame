package cc.mewcraft.wakame.item.templates

import cc.mewcraft.wakame.item.templates.components.moduleComponents
import cc.mewcraft.wakame.item.templates.filters.moduleFilters
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun templatesModule(): Module = module {
    includes(
        moduleComponents(),
        moduleFilters()
    )
}