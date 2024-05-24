package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.binary.cell.binaryCellModule
import cc.mewcraft.wakame.item.binary.meta.binaryItemMetaModule
import cc.mewcraft.wakame.item.binary.show.itemShowModule
import cc.mewcraft.wakame.item.binary.stats.itemStatisticsModule
import org.koin.core.module.Module
import org.koin.dsl.module

internal fun binaryItemModule(): Module = module {
    includes(
        binaryCellModule(),
        binaryItemMetaModule(),
        itemShowModule(),
        itemStatisticsModule(),
    )
}