package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.wakame.item.components.cell.curses.CurseEmpty
import cc.mewcraft.wakame.item.components.cell.curses.CurseEntityKills
import cc.mewcraft.wakame.item.components.cell.curses.CursePeakDamage

object CurseTypes {
    val EMPTY: CurseType<CurseEmpty> = CurseEmpty
    val ENTITY_KILLS: CurseType<CurseEntityKills> = CurseEntityKills.Type
    val PEAK_DAMAGE: CurseType<CursePeakDamage> = CursePeakDamage.Type

    private fun <T : Curse> dummy(): CurseType<T> {
        TODO()
    }
}