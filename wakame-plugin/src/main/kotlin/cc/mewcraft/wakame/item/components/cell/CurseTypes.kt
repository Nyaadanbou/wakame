package cc.mewcraft.wakame.item.components.cell

import cc.mewcraft.wakame.item.components.cell.curses.CurseEmpty
import cc.mewcraft.wakame.item.components.cell.curses.CurseEntityKills
import cc.mewcraft.wakame.item.components.cell.curses.CursePeakDamage

/**
 * 诅咒的所有类型.
 */
object CurseTypes {
    /**
     * 空 (技术诅咒).
     *
     * 当一个词条栏里没有诅咒时 (但词条栏本身存在), 里面实际上有一个空诅咒.
     *
     * 玩家概念上的“词条栏没有诅咒”, 就是技术概念上的 “词条栏里装的是空诅咒”.
     */
    val EMPTY: CurseType<CurseEmpty> = CurseEmpty

    /**
     * 实体击杀数.
     */
    val ENTITY_KILLS: CurseType<CurseEntityKills> = CurseEntityKills.Type

    /**
     * 单次最高伤害.
     */
    val PEAK_DAMAGE: CurseType<CursePeakDamage> = CursePeakDamage.Type
}