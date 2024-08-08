package cc.mewcraft.wakame.gui.merge

import cc.mewcraft.wakame.reforge.merging.MergingTable
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

class MergingMenu(
    val table: MergingTable,
    val viewer: Player,
) : KoinComponent {

    fun open() {

    }
}