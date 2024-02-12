package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.core.BinaryCore
import cc.mewcraft.wakame.item.binary.core.emptyBinaryCore
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.curse.emptyBinaryCurse
import cc.mewcraft.wakame.util.compoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag

@InternalApi
internal object EmptyBinaryCell : BinaryCell {
    override val canReforge: Boolean = false
    override val canOverride: Boolean = false
    override val binaryCore: BinaryCore = emptyBinaryCore()
    override val binaryCurse: BinaryCurse = emptyBinaryCurse()
    override val reforgeMeta: ReforgeMeta = emptyReforgeMeta()

    override fun asShadowTag(): ShadowTag = compoundShadowTag {
        // FIXME how does the structure of empty binary core look?
    }
}