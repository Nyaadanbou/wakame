package cc.mewcraft.wakame.item.binary.cell.reforge

import me.lucko.helper.shadows.nbt.CompoundShadowTag

object ReforgeDataFactory {
    /**
     * Creates an empty reforge meta.
     */
    fun empty(): ReforgeDataHolder {
        return ReforgeDataHolderImpl(CompoundShadowTag.create())
    }

    /**
     * Creates a reforge meta from a NBT source.
     *
     * @param compound the compound
     * @return a new instance
     */
    fun wrap(compound: CompoundShadowTag): ReforgeDataHolder {
        if (compound.isEmpty) {
            return empty()
        }

        return ReforgeDataHolderImpl(compound)
    }
}