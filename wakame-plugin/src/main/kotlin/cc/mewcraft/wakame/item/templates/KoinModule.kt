package cc.mewcraft.wakame.item.templates

import cc.mewcraft.wakame.item.templates.filter.filterModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun templatesModule(): Module = module {
    includes(
        filterModule()
    )
}