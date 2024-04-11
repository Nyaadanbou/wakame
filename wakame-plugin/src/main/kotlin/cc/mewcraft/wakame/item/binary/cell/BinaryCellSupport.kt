package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.CellBinaryKeys
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.core.isNotEmpty
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.isNotEmpty
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag

data class ImmutableBinaryCell(
    override val id: String,
    override val isReforgeable: Boolean,
    override val isOverridable: Boolean,
    override val core: BinaryCore,
    override val curse: BinaryCurse,
    override val reforgeData: ReforgeData,
) : BinaryCell {

    override fun asShadowTag(): ShadowTag = CompoundShadowTag {
        putBoolean(CellBinaryKeys.REFORGEABLE, isReforgeable)
        putBoolean(CellBinaryKeys.OVERRIDABLE, isOverridable)

        // If those instances are empty, we simply don't write them into the NBT
        putIfTrue(CoreBinaryKeys.BASE, core.asShadowTag()) { core.isNotEmpty }
        putIfTrue(CurseBinaryKeys.BASE, curse.asShadowTag()) { curse.isNotEmpty }
        putIfTrue(ReforgeBinaryKeys.BASE, reforgeData.asShadowTag()) { reforgeData.isNotEmpty }
    }

    private fun CompoundShadowTag.putIfTrue(key: String, value: ShadowTag, predicate: () -> Boolean) {
        if (predicate()) {
            put(key, value)
        }
    }

}