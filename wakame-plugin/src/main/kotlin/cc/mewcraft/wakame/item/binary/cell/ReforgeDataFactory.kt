package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import me.lucko.helper.shadows.nbt.CompoundShadowTag

object ReforgeDataFactory {
    /**
     * Creates an empty reforge meta.
     */
    fun empty(): ReforgeData = EmptyReforgeData

    /**
     * Creates a reforge meta from a NBT source.
     *
     * @param compound the compound
     * @return a new instance
     */
    fun decode(compound: CompoundShadowTag): ReforgeData {
        if (compound.isEmpty) {
            return empty()
        }

        return ImmutableReforgeData(
            successCount = compound.getInt(ReforgeBinaryKeys.SUCCESS_COUNT),
            failureCount = compound.getInt(ReforgeBinaryKeys.FAILURE_COUNT),
        )
    }
}