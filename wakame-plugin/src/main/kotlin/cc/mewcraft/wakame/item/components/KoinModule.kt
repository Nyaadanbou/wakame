package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.components.cell.cellModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun componentsModule(): Module = module {
    includes(
        cellModule()
    )
}