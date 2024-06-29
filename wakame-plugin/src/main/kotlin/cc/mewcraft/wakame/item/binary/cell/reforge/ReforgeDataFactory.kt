package cc.mewcraft.wakame.item.binary.cell.reforge

import cc.mewcraft.nbt.CompoundTag

object ReforgeDataFactory {
    /**
     * Creates an empty reforge meta.
     */
    fun empty(): ReforgeDataHolder {
        return ReforgeDataHolderImpl(CompoundTag.create())
    }

    /**
     * Creates a reforge meta from a NBT source.
     *
     * @param compound the compound
     * @return a new instance
     */
    fun wrap(compound: CompoundTag): ReforgeDataHolder {
        if (compound.isEmpty) {
            return empty()
        }

        return ReforgeDataHolderImpl(compound)
    }
}