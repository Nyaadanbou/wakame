package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
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
        putBoolean(NekoTags.Cell.CAN_REFORGE, isReforgeable)
        putBoolean(NekoTags.Cell.CAN_OVERRIDE, isOverridable)

        // If those instances are empty, we simply don't write them into the NBT
        putIfTrue(NekoTags.Cell.CORE, core.asShadowTag()) { core.isNotEmpty }
        putIfTrue(NekoTags.Cell.CURSE, curse.asShadowTag()) { curse.isNotEmpty }
        putIfTrue(NekoTags.Cell.REFORGE, reforgeData.asShadowTag()) { reforgeData.isNotEmpty }
    }

    private fun CompoundShadowTag.putIfTrue(key: String, value: ShadowTag, predicate: () -> Boolean) {
        if (predicate()) {
            put(key, value)
        }
    }

}