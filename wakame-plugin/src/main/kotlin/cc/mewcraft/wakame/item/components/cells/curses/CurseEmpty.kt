package cc.mewcraft.wakame.item.components.cells.curses

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.Curse
import cc.mewcraft.wakame.item.components.cells.CurseType
import net.kyori.adventure.key.Key

/**
 * 空的诅咒. 相当于永远是解锁状态.
 */
data object CurseEmpty : Curse, CurseType<CurseEmpty> {
    override val key: Key = GenericKeys.EMPTY
    override val type: CurseType<CurseEmpty> = this
    override val isEmpty: Boolean = true
    override fun isLocked(context: NekoStack): Boolean = false
    override fun isUnlocked(context: NekoStack): Boolean = true
    override fun serializeAsTag(): Tag = CompoundTag.create()
}