package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.binary.core.BinaryCore
import cc.mewcraft.wakame.item.binary.core.isNotEmpty
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.isNotEmpty
import cc.mewcraft.wakame.util.compoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag

internal data class BinaryCellImpl(
    override val canReforge: Boolean,
    override val canOverride: Boolean,
    override val binaryCore: BinaryCore,
    override val binaryCurse: BinaryCurse,
    override val reforgeMeta: ReforgeMeta,
) : BinaryCell {

    override fun asShadowTag(): ShadowTag = compoundShadowTag {
        putBoolean(NekoTags.Cell.CAN_REFORGE, canReforge)
        putBoolean(NekoTags.Cell.CAN_OVERRIDE, canOverride)

        // If those instances are empty, we simply don't write them into the NBT
        putIfTrue(NekoTags.Cell.CORE, binaryCore.asShadowTag()) { binaryCore.isNotEmpty }
        putIfTrue(NekoTags.Cell.CURSE, binaryCurse.asShadowTag()) { binaryCurse.isNotEmpty }
        putIfTrue(NekoTags.Cell.REFORGE, reforgeMeta.asShadowTag()) { reforgeMeta.isNotEmpty }
    }

    private fun CompoundShadowTag.putIfTrue(key: String, value: ShadowTag, predicate: () -> Boolean) {
        if (predicate()) {
            put(key, value)
        }
    }

}