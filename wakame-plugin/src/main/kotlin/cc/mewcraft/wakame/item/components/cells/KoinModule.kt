package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.wakame.item.components.cells.cores.moduleCores
import cc.mewcraft.wakame.item.components.cells.reforge.moduleReforge
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun moduleCells(): Module = module {
    includes(
        moduleCores(),
        moduleReforge()
    )
}