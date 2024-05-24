package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.binary.cell.core.binaryCoreModule
import cc.mewcraft.wakame.item.binary.cell.curse.binaryCurseModule
import cc.mewcraft.wakame.item.binary.cell.reforge.reforgeModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun binaryCellModule(): Module = module {
    includes(
        binaryCoreModule(),
        binaryCurseModule(),
        reforgeModule(),
    )
}