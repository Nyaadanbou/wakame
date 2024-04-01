package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.NekoTags
import me.lucko.helper.shadows.nbt.CompoundShadowTag

object ReforgeMetaFactory {
    /**
     * Creates an empty reforge meta.
     */
    fun empty(): ReforgeMeta = EmptyReforgeMeta

    /**
     * Creates a reforge meta from a NBT source.
     *
     * @param compound the compound
     * @return a new instance
     */
    fun decode(compound: CompoundShadowTag): ReforgeMeta {
        if (compound.isEmpty) {
            return empty()
        }

        return ImmutableReforgeMeta(
            successCount = compound.getInt(NekoTags.Reforge.SUCCESS),
            failureCount = compound.getInt(NekoTags.Reforge.FAILURE)
        )
    }
}