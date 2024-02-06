package cc.mewcraft.wakame.item.binary.curse

import cc.mewcraft.wakame.item.binary.cell.CellAccessor
import cc.mewcraft.wakame.item.binary.meta.ItemMetaAccessor
import cc.mewcraft.wakame.item.binary.stats.ItemStatsAccessor

/**
 * The context for [BinaryCurse] to check with.
 */
interface BinaryCurseContext {
    val cellContext: CellAccessor
    val metaContext: ItemMetaAccessor
    val statsContext: ItemStatsAccessor
}
