package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.CurseBinaryKeys
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCoreFactory
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurseFactory
import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataFactory
import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataHolder
import cc.mewcraft.wakame.util.CompoundShadowTag
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import me.lucko.helper.shadows.nbt.ShadowTag

internal data class BinaryCellDataHolder(
    override val core: BinaryCore,
    override val curse: BinaryCurse,
    override val reforge: ReforgeDataHolder,
) : BinaryCell {
    override fun asShadowTag(): ShadowTag =
        CompoundShadowTag {
            // 当对应的数据不存在时，这里的每个 asShadowTag()
            // 应该返回一个没有内容的空 Compound（不是 null）
            put(CoreBinaryKeys.BASE, core.asShadowTag())
            put(CurseBinaryKeys.BASE, curse.asShadowTag())
            put(ReforgeBinaryKeys.BASE, reforge.asShadowTag())
        }
}

internal data class BinaryCellNBTWrapper(
    private val compound: CompoundShadowTag,
) : BinaryCell {
    override val core: BinaryCore
        get() = BinaryCoreFactory.wrap(compound.getCompound(CoreBinaryKeys.BASE))
    override val curse: BinaryCurse
        get() = BinaryCurseFactory.wrap(compound.getCompound(CurseBinaryKeys.BASE))
    override val reforge: ReforgeDataHolder
        get() = ReforgeDataFactory.wrap(compound.getCompound(ReforgeBinaryKeys.BASE))

    override fun asShadowTag(): ShadowTag = compound
    override fun toString(): String = compound.asString()
}
