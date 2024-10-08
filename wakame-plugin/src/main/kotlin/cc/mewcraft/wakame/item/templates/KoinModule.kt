package cc.mewcraft.wakame.item.templates

import cc.mewcraft.wakame.item.templates.components.componentsModule
import cc.mewcraft.wakame.item.templates.filters.filtersModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun templatesModule(): Module = module {
    includes(
        componentsModule(),
        filtersModule()
    )
}